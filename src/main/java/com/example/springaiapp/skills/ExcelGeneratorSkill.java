package com.example.springaiapp.skills;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.function.FunctionToolCallback;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;

/**
 * Excel生成工具
 * 分析模板Excel结构，结合用户上下文通过AI生成新数据并写入新文件
 *
 * @author Gabriel
 * @version 1.0
 */
@Slf4j
@Component
public class ExcelGeneratorSkill implements BiFunction<ExcelGeneratorSkill.GeneratorRequest, ToolContext, String> {

    private final ChatModel chatModel;

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

    /**
     * 构造函数注入ChatModel（使用@Lazy打破循环依赖）
     *
     * @param chatModel AI聊天模型
     */
    public ExcelGeneratorSkill(@Lazy ChatModel chatModel) {
        this.chatModel = chatModel;
    }

    /**
     * 根据模板生成Excel文件
     *
     * @param request 生成请求参数
     * @param context 工具上下文
     * @return 生成结果JSON字符串
     */
    @Override
    public String apply(GeneratorRequest request, ToolContext context) {
        log.info("开始根据模板生成Excel: {} -> {}", request.templatePath, request.outputPath);

        try {
            String templatePath = request.templatePath;
            String outputPath = request.outputPath;
            String sheetName = request.sheetName;
            String userContext = request.userContext;
            int generateRows = request.generateRows != null ? request.generateRows : 10;
            boolean preserveStyle = request.preserveStyle != null ? request.preserveStyle : true;
            Map<String, Object> cellMapping = request.cellMapping;

            if (cellMapping != null && !cellMapping.isEmpty()) {
                ContractTemplateProcessor.generateContract(templatePath, outputPath, cellMapping, 
                        request.clearStartCell, request.clearEndCell);
            } else {
                Map<String, Object> templateInfo = analyzeTemplate(templatePath, sheetName);
                List<Map<String, Object>> generatedData = generateDataByAI(templateInfo, userContext, generateRows);
                String resultSheetName = (String) templateInfo.get("sheetName");
                List<String> headers = (List<String>) templateInfo.get("headers");

                if (preserveStyle) {
                    writeExcelWithStyle(templatePath, outputPath, resultSheetName, headers, generatedData);
                } else {
                    writeExcel(outputPath, resultSheetName, headers, generatedData);
                }
            }

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("templatePath", templatePath);
            response.put("outputPath", outputPath);
            response.put("cellMapping", cellMapping != null);
            response.put("message", "Excel文件生成成功");

            log.info("Excel文件生成成功，使用单元格映射: {}", cellMapping != null);
            return toJson(response);

        } catch (Exception e) {
            log.error("生成Excel文件失败: {}", e.getMessage(), e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            return toJson(errorResponse);
        }
    }

    /**
     * 分析模板文件结构
     *
     * @param templatePath 模板文件路径
     * @param sheetName    工作表名称（可选）
     * @return 模板信息
     */
    private Map<String, Object> analyzeTemplate(String templatePath, String sheetName) throws IOException {
        Map<String, Object> templateInfo = new HashMap<>();

        try (FileInputStream fis = new FileInputStream(templatePath);
             Workbook workbook = new XSSFWorkbook(fis)) {

            Sheet sheet;
            if (sheetName != null && !sheetName.isEmpty()) {
                sheet = workbook.getSheet(sheetName);
                if (sheet == null) {
                    sheet = workbook.getSheetAt(0);
                    log.warn("找不到工作表[{}]，使用第一个工作表", sheetName);
                }
            } else {
                sheet = workbook.getSheetAt(0);
            }

            templateInfo.put("sheetName", sheet.getSheetName());
            templateInfo.put("totalRows", sheet.getLastRowNum() + 1);

            Row headerRow = sheet.getRow(0);
            List<String> headers = new ArrayList<>();
            List<Map<String, Object>> columnInfo = new ArrayList<>();

            if (headerRow != null) {
                int lastCellNum = headerRow.getLastCellNum();
                for (int i = 0; i < lastCellNum; i++) {
                    Cell cell = headerRow.getCell(i);
                    String headerName = getCellValueAsString(cell);
                    headers.add(headerName);

                    Map<String, Object> colInfo = new HashMap<>();
                    colInfo.put("index", i);
                    colInfo.put("name", headerName);

                    Map<String, Integer> typeCount = new HashMap<>();
                    Set<String> sampleValues = new HashSet<>();

                    for (int r = 1; r <= Math.min(sheet.getLastRowNum(), 10); r++) {
                        Row dataRow = sheet.getRow(r);
                        if (dataRow != null) {
                            Cell dataCell = dataRow.getCell(i);
                            String typeName = getCellTypeName(dataCell);
                            typeCount.merge(typeName, 1, Integer::sum);

                            String sampleValue = getCellValueAsString(dataCell);
                            if (!sampleValue.isEmpty() && sampleValues.size() < 5) {
                                sampleValues.add(sampleValue);
                            }
                        }
                    }

                    String inferredType = inferColumnType(typeCount);
                    colInfo.put("type", inferredType);
                    colInfo.put("samples", new ArrayList<>(sampleValues));

                    columnInfo.add(colInfo);
                }
            }

            templateInfo.put("headers", headers);
            templateInfo.put("columnInfo", columnInfo);
        }

        return templateInfo;
    }

    /**
     * 推断列数据类型
     */
    private String inferColumnType(Map<String, Integer> typeCount) {
        if (typeCount.isEmpty()) {
            return "STRING";
        }

        String maxType = "STRING";
        int maxCount = 0;

        for (Map.Entry<String, Integer> entry : typeCount.entrySet()) {
            if (entry.getValue() > maxCount && !"BLANK".equals(entry.getKey())) {
                maxCount = entry.getValue();
                maxType = entry.getKey();
            }
        }

        return maxType;
    }

    /**
     * 通过AI生成数据
     *
     * @param templateInfo 模板信息
     * @param userContext  用户上下文
     * @param rowCount     生成行数
     * @return 生成的数据列表
     */
    private List<Map<String, Object>> generateDataByAI(Map<String, Object> templateInfo,
                                                        String userContext, int rowCount) {
        List<String> headers = (List<String>) templateInfo.get("headers");
        List<Map<String, Object>> columnInfo = (List<Map<String, Object>>) templateInfo.get("columnInfo");

        StringBuilder promptBuilder = new StringBuilder();
        promptBuilder.append("你是一个数据生成助手。请根据以下模板信息生成").append(rowCount).append("条数据。\n\n");
        promptBuilder.append("## 模板列信息:\n");

        for (Map<String, Object> col : columnInfo) {
            promptBuilder.append("- 列名: ").append(col.get("name"));
            promptBuilder.append(", 类型: ").append(col.get("type"));
            List<String> samples = (List<String>) col.get("samples");
            if (samples != null && !samples.isEmpty()) {
                promptBuilder.append(", 示例值: ").append(samples);
            }
            promptBuilder.append("\n");
        }

        promptBuilder.append("\n## 用户需求:\n");
        promptBuilder.append(userContext).append("\n");

        promptBuilder.append("\n## 输出要求:\n");
        promptBuilder.append("请严格按照JSON数组格式输出，不要包含任何其他文字说明。\n");
        promptBuilder.append("每个元素是一个对象，键名为列名，键值为对应的数据。\n");
        promptBuilder.append("示例格式: [{\"列名1\": \"值1\", \"列名2\": \"值2\"}, {\"列名1\": \"值3\", \"列名2\": \"值4\"}]\n");

        String promptText = promptBuilder.toString();
        log.debug("生成数据的Prompt: {}", promptText);

        try {
            Prompt prompt = new Prompt(new UserMessage(promptText));
            String response = chatModel.call(prompt).getResult().getOutput().getText();

            log.debug("AI响应: {}", response);

            String jsonContent = extractJsonArray(response);

            List<Map<String, Object>> data = OBJECT_MAPPER.readValue(jsonContent,
                    new TypeReference<List<Map<String, Object>>>() {});

            return data;

        } catch (Exception e) {
            log.error("AI生成数据失败: {}", e.getMessage(), e);
            throw new RuntimeException("AI生成数据失败: " + e.getMessage());
        }
    }

    /**
     * 从响应中提取JSON数组
     */
    private String extractJsonArray(String response) {
        int start = response.indexOf('[');
        int end = response.lastIndexOf(']');

        if (start >= 0 && end > start) {
            return response.substring(start, end + 1);
        }

        throw new RuntimeException("无法从AI响应中提取JSON数组");
    }

    /**
     * 写入Excel文件（保留模板样式）
     */
    private void writeExcelWithStyle(String templatePath, String filePath,
                                      String sheetName, List<String> headers,
                                      List<Map<String, Object>> data) throws IOException {
        ExcelStyleHelper.generateExcelWithFullStyle(templatePath, filePath, sheetName, 1, 0, data);
    }

    /**
     * 写入Excel文件（不保留样式）
     */
    private void writeExcel(String filePath, String sheetName,
                            List<String> headers, List<Map<String, Object>> data) throws IOException {

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet(sheetName);

            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle dateStyle = workbook.createCellStyle();
            CreationHelper creationHelper = workbook.getCreationHelper();
            dateStyle.setDataFormat(creationHelper.createDataFormat().getFormat("yyyy-mm-dd"));

            if (headers != null && !headers.isEmpty()) {
                Row headerRow = sheet.createRow(0);
                for (int i = 0; i < headers.size(); i++) {
                    Cell cell = headerRow.createCell(i);
                    cell.setCellValue(headers.get(i) != null ? headers.get(i) : "");
                    cell.setCellStyle(headerStyle);
                }
            }

            for (int i = 0; i < data.size(); i++) {
                Row row = sheet.createRow(i + 1);
                Map<String, Object> rowData = data.get(i);

                if (rowData != null && headers != null) {
                    for (int j = 0; j < headers.size(); j++) {
                        Cell cell = row.createCell(j);
                        String headerName = headers.get(j);
                        Object value = rowData.get(headerName);
                        setCellValue(cell, value, dateStyle);
                    }
                }
            }

            for (int i = 0; i < (headers != null ? headers.size() : 10); i++) {
                sheet.autoSizeColumn(i);
            }

            try (FileOutputStream fos = new FileOutputStream(filePath)) {
                workbook.write(fos);
            }
        }
    }

    /**
     * 创建表头样式
     */
    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setAlignment(HorizontalAlignment.CENTER);

        Font font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);

        return style;
    }

    /**
     * 设置单元格值
     */
    private void setCellValue(Cell cell, Object value, CellStyle dateStyle) {
        if (value == null) {
            cell.setCellValue("");
        } else if (value instanceof String) {
            cell.setCellValue((String) value);
        } else if (value instanceof Number) {
            cell.setCellValue(((Number) value).doubleValue());
        } else if (value instanceof Boolean) {
            cell.setCellValue((Boolean) value);
        } else if (value instanceof java.util.Date) {
            cell.setCellValue((java.util.Date) value);
            cell.setCellStyle(dateStyle);
        } else {
            cell.setCellValue(value.toString());
        }
    }

    /**
     * 获取单元格值作为字符串
     */
    private String getCellValueAsString(Cell cell) {
        if (cell == null) {
            return "";
        }

        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return DATE_FORMAT.format(cell.getDateCellValue());
                }
                double numVal = cell.getNumericCellValue();
                if (numVal == Math.floor(numVal)) {
                    return String.valueOf((long) numVal);
                }
                return String.valueOf(numVal);
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                return cell.toString();
            case BLANK:
                return "";
            default:
                return cell.toString();
        }
    }

    /**
     * 获取单元格类型名称
     */
    private String getCellTypeName(Cell cell) {
        if (cell == null) {
            return "BLANK";
        }

        CellType type = cell.getCellType();
        if (type == CellType.FORMULA) {
            type = cell.getCachedFormulaResultType();
        }

        if (type == CellType.NUMERIC && DateUtil.isCellDateFormatted(cell)) {
            return "DATE";
        }

        switch (type) {
            case STRING:
                return "STRING";
            case NUMERIC:
                return "NUMERIC";
            case BOOLEAN:
                return "BOOLEAN";
            case FORMULA:
                return "FORMULA";
            case BLANK:
                return "BLANK";
            default:
                return "UNKNOWN";
        }
    }

    /**
     * 简单的JSON序列化
     */
    private String toJson(Map<String, Object> map) {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        boolean first = true;
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            if (!first) {
                sb.append(", ");
            }
            first = false;
            sb.append("\"").append(entry.getKey()).append("\": ");
            Object value = entry.getValue();
            if (value == null) {
                sb.append("null");
            } else if (value instanceof String) {
                sb.append("\"").append(escapeJson((String) value)).append("\"");
            } else if (value instanceof List) {
                sb.append(listToJson((List<?>) value));
            } else {
                sb.append(value);
            }
        }
        sb.append("}");
        return sb.toString();
    }

    /**
     * List转JSON
     */
    private String listToJson(List<?> list) {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        boolean first = true;
        for (Object item : list) {
            if (!first) {
                sb.append(", ");
            }
            first = false;
            if (item == null) {
                sb.append("null");
            } else if (item instanceof String) {
                sb.append("\"").append(escapeJson((String) item)).append("\"");
            } else {
                sb.append(item);
            }
        }
        sb.append("]");
        return sb.toString();
    }

    /**
     * JSON字符串转义
     */
    private String escapeJson(String str) {
        return str.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    /**
     * 创建工具回调（实例方法）
     *
     * @return ToolCallback 工具回调
     */
    public ToolCallback createToolCallback() {
        return FunctionToolCallback.builder("excel_generator", this)
                .description("根据模板Excel生成新文件。参数包括: templatePath模板文件路径(必填), " +
                        "outputPath输出文件路径(必填), userContext用户需求描述(必填,描述要生成什么样的数据), " +
                        "sheetName工作表名称(可选), generateRows生成行数(可选,默认10), " +
                        "preserveStyle是否保留模板样式(可选,默认true), " +
                        "cellMapping单元格映射(可选,Map格式,key为单元格位置如\"A1\",value为值)。" +
                        "提供cellMapping时直接使用ContractTemplateProcessor完美保留所有样式；否则使用AI生成数据。")
                .inputType(GeneratorRequest.class)
                .build();
    }

    /**
     * 生成请求参数
     */
    public static class GeneratorRequest {
        @JsonProperty(required = true)
        @JsonPropertyDescription("模板Excel文件的完整路径")
        public String templatePath;

        @JsonProperty(required = true)
        @JsonPropertyDescription("输出Excel文件的完整路径")
        public String outputPath;

        @JsonProperty
        @JsonPropertyDescription("用户需求描述，说明要生成什么样的数据，例如：生成10条员工信息，姓名为中文，年龄在25-40之间")
        public String userContext;

        @JsonProperty
        @JsonPropertyDescription("工作表名称，不填则使用第一个工作表")
        public String sheetName;

        @JsonProperty
        @JsonPropertyDescription("生成数据行数，默认10")
        public Integer generateRows;

        @JsonProperty
        @JsonPropertyDescription("是否保留模板样式，默认true")
        public Boolean preserveStyle;

        @JsonProperty
        @JsonPropertyDescription("单元格映射，Map格式，key为单元格位置如\"A1\"，value为值。提供此参数时直接填充数据，完美保留所有样式")
        public Map<String, Object> cellMapping;

        @JsonProperty
        @JsonPropertyDescription("要清除区域的起始单元格（如\"A8\"），配合cellMapping使用，清除模板原有数据")
        public String clearStartCell;

        @JsonProperty
        @JsonPropertyDescription("要清除区域的结束单元格（如\"G11\"），配合cellMapping使用，清除模板原有数据")
        public String clearEndCell;
    }
}
