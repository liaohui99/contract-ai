package com.example.springaiapp.skills;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.function.FunctionToolCallback;
import org.springframework.stereotype.Component;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

/**
 * Excel写入工具
 * 用于Agent调用写入数据到Excel文件
 * 支持.xlsx格式（Excel 2007+）
 *
 * @author Gabriel
 * @version 1.0
 */
@Slf4j
@Component
public class ExcelWriteTool implements BiFunction<ExcelWriteTool.WriteRequest, ToolContext, String> {

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    /**
     * 写入Excel文件
     *
     * @param request 写入请求参数
     * @param context 工具上下文
     * @return 写入结果JSON字符串
     */
    @Override
    public String apply(WriteRequest request, ToolContext context) {
        log.info("开始写入Excel文件: {}", request.filePath);

        try {
            // 验证必需参数
            if (request == null) {
                throw new IllegalArgumentException("请求参数不能为空");
            }
            if (request.filePath == null || request.filePath.trim().isEmpty()) {
                throw new IllegalArgumentException("文件路径不能为空");
            }

            String filePath = request.filePath;
            String sheetName = request.sheetName != null ? request.sheetName : "Sheet1";
            List<String> headers = request.headers;
            List<List<Object>> data = request.data;
            Boolean appendMode = request.appendMode;
            String templatePath = request.templatePath;
            Map<String, Object> cellMapping = request.cellMapping;

            // 确保appendMode有默认值
            if (appendMode == null) {
                appendMode = false;
            }

            boolean autoProtected = false;
            java.io.File targetFile = new java.io.File(filePath);
            
            if (templatePath != null && !templatePath.isEmpty() && cellMapping != null && !cellMapping.isEmpty()) {
                ContractTemplateProcessor.generateContract(templatePath, filePath, cellMapping, 
                        request.clearStartCell, request.clearEndCell);
            } else {
                if (!appendMode && (templatePath == null || templatePath.isEmpty()) 
                        && targetFile.exists() && targetFile.length() > 0) {
                    templatePath = filePath;
                    autoProtected = true;
                    log.info("检测到文件已存在且非追加模式，自动使用原文件作为模板以保护原有内容");
                }
                writeExcel(filePath, sheetName, headers, data, appendMode, templatePath);
            }

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("filePath", filePath);
            response.put("sheetName", sheetName);
            if (cellMapping != null && !cellMapping.isEmpty()) {
                response.put("cellMapping", true);
            } else if (data != null) {
                response.put("totalRows", data.size());
            }
            if (templatePath != null && !templatePath.isEmpty()) {
                response.put("templatePath", templatePath);
                response.put("preserveStyle", true);
            }
            if (autoProtected) {
                response.put("autoProtected", true);
                response.put("message", "Excel文件写入成功（已自动保护原有内容）");
            } else {
                response.put("message", "Excel文件写入成功");
            }

            log.info("Excel文件写入成功，使用单元格映射: {}, 自动保护: {}", cellMapping != null, autoProtected);
            return toJson(response);

        } catch (Exception e) {
            log.error("写入Excel文件失败: {}", e.getMessage(), e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            errorResponse.put("message", "写入Excel文件失败: " + e.getMessage());
            return toJson(errorResponse);
        }
    }

    /**
     * 写入Excel文件
     *
     * @param filePath   文件路径
     * @param sheetName  工作表名称
     * @param headers    表头列表
     * @param data       数据列表
     * @param appendMode 是否追加模式
     * @param templatePath 模板文件路径（可选）
     */
    private void writeExcel(String filePath, String sheetName,
                            List<String> headers, List<List<Object>> data,
                            boolean appendMode, String templatePath) throws IOException {

        if (templatePath != null && !templatePath.isEmpty()) {
            writeExcelWithTemplate(templatePath, filePath, sheetName, headers, data, appendMode);
            return;
        }

        Workbook workbook;
        Sheet sheet;
        int startRowNum = 0;

        if (appendMode) {
            workbook = openOrCreateWorkbook(filePath);
            sheet = workbook.getSheet(sheetName);
            if (sheet == null) {
                sheet = workbook.createSheet(sheetName);
            } else {
                startRowNum = sheet.getLastRowNum() + 1;
            }
        } else {
            workbook = new XSSFWorkbook();
            sheet = workbook.createSheet(sheetName);
        }

        CellStyle headerStyle = createHeaderStyle(workbook);
        CellStyle dateStyle = workbook.createCellStyle();
        CreationHelper creationHelper = workbook.getCreationHelper();
        dateStyle.setDataFormat(creationHelper.createDataFormat().getFormat("yyyy-mm-dd"));

        if (headers != null && !headers.isEmpty() && !appendMode) {
            Row headerRow = sheet.createRow(startRowNum++);
            for (int i = 0; i < headers.size(); i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers.get(i) != null ? headers.get(i) : "");
                cell.setCellStyle(headerStyle);
            }
        }

        for (int i = 0; i < data.size(); i++) {
            Row row = sheet.createRow(startRowNum + i);
            List<Object> rowData = data.get(i);

            if (rowData != null) {
                for (int j = 0; j < rowData.size(); j++) {
                    Cell cell = row.createCell(j);
                    setCellValue(cell, rowData.get(j), dateStyle);
                }
            }
        }

        for (int i = 0; i < (headers != null ? headers.size() : 10); i++) {
            sheet.autoSizeColumn(i);
        }

        // 自适应行高
        if (data != null) {
            autoFitRowHeights(sheet, data, startRowNum);
        }

        try (FileOutputStream fos = new FileOutputStream(filePath)) {
            workbook.write(fos);
        }

        workbook.close();
    }

    /**
     * 使用模板写入Excel文件，保留所有样式
     * 支持模板路径和输出路径相同的情况（原地编辑）
     */
    private void writeExcelWithTemplate(String templatePath, String outputPath,
                                        String sheetName, List<String> headers,
                                        List<List<Object>> data, boolean appendMode) throws IOException {
        Workbook workbook;
        try (FileInputStream fis = new FileInputStream(templatePath)) {
            workbook = new XSSFWorkbook(fis);
        }

        try {
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

            int startRowNum;
            if (appendMode) {
                startRowNum = sheet.getLastRowNum() + 1;
            } else {
                int headerRowIndex = 0;
                if (headers != null && !headers.isEmpty()) {
                    headerRowIndex = 1;
                }
                startRowNum = headerRowIndex;
                clearDataFromRow(sheet, startRowNum);
            }

            CellStyle dateStyle = workbook.createCellStyle();
            CreationHelper creationHelper = workbook.getCreationHelper();
            dateStyle.setDataFormat(creationHelper.createDataFormat().getFormat("yyyy-mm-dd"));

            for (int i = 0; i < data.size(); i++) {
                int rowNum = startRowNum + i;
                Row row = getOrCreateRow(sheet, rowNum);
                List<Object> rowData = data.get(i);

                if (rowData != null) {
                    for (int j = 0; j < rowData.size(); j++) {
                        Cell cell = row.getCell(j, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
                        setCellValueWithStyle(cell, rowData.get(j), dateStyle);
                    }
                }
            }

            // 自适应行高
            if (data != null) {
                autoFitRowHeights(sheet, data, startRowNum);
            }

            try (FileOutputStream fos = new FileOutputStream(outputPath)) {
                workbook.write(fos);
            }
        } finally {
            workbook.close();
        }
    }

    /**
     * 获取或创建行
     */
    private Row getOrCreateRow(Sheet sheet, int rowNum) {
        Row row = sheet.getRow(rowNum);
        if (row == null) {
            row = sheet.createRow(rowNum);
        }
        return row;
    }

    /**
     * 清空从指定行开始的数据（保留样式）
     */
    private void clearDataFromRow(Sheet sheet, int startRow) {
        int lastRowNum = sheet.getLastRowNum();
        for (int i = lastRowNum; i >= startRow; i--) {
            Row row = sheet.getRow(i);
            if (row != null) {
                for (int j = 0; j < row.getLastCellNum(); j++) {
                    Cell cell = row.getCell(j);
                    if (cell != null) {
                        cell.setBlank();
                    }
                }
            }
        }
    }

    /**
     * 设置单元格值（保留原有样式）
     */
    private void setCellValueWithStyle(Cell cell, Object value, CellStyle dateStyle) {
        if (value == null) {
            cell.setBlank();
            return;
        }

        if (value instanceof String) {
            cell.setCellValue((String) value);
        } else if (value instanceof Number) {
            cell.setCellValue(((Number) value).doubleValue());
        } else if (value instanceof Boolean) {
            cell.setCellValue((Boolean) value);
        } else if (value instanceof Date) {
            cell.setCellValue((Date) value);
            if (dateStyle != null) {
                CellStyle originalStyle = cell.getCellStyle();
                if (originalStyle != null) {
                    CellStyle newStyle = cell.getSheet().getWorkbook().createCellStyle();
                    newStyle.cloneStyleFrom(originalStyle);
                    newStyle.setDataFormat(dateStyle.getDataFormat());
                    cell.setCellStyle(newStyle);
                } else {
                    cell.setCellStyle(dateStyle);
                }
            }
        } else {
            cell.setCellValue(value.toString());
        }
    }

    /**
     * 打开或创建工作簿
     */
    private Workbook openOrCreateWorkbook(String filePath) throws IOException {
        java.io.File file = new java.io.File(filePath);
        if (file.exists() && file.length() > 0) {
            return new XSSFWorkbook(new java.io.FileInputStream(file));
        }
        return new XSSFWorkbook();
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
        } else if (value instanceof Date) {
            cell.setCellValue((Date) value);
            cell.setCellStyle(dateStyle);
        } else {
            cell.setCellValue(value.toString());
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
            } else {
                sb.append(value);
            }
        }
        sb.append("}");
        return sb.toString();
    }

    /**
     * 自适应行高度
     */
    private void autoFitRowHeights(Sheet sheet, List<List<Object>> data, int startRowNum) {
        if (data == null) return;
        
        for (int i = 0; i < data.size(); i++) {
            Row row = sheet.getRow(startRowNum + i);
            if (row != null) {
                // 计算这一行的最大内容高度
                float maxHeight = 0;
                List<Object> rowData = data.get(i);
                
                for (int j = 0; j < rowData.size(); j++) {
                    Object cellValue = rowData.get(j);
                    if (cellValue != null) {
                        String strValue = cellValue.toString();
                        // 简单估算：每行约20个字符换行，每行增加约15像素高度
                        int lineCount = Math.max(1, (int) Math.ceil(strValue.length() / 20.0));
                        float estimatedHeight = lineCount * 15.0f;
                        if (estimatedHeight > maxHeight) {
                            maxHeight = estimatedHeight;
                        }
                    }
                }
                
                // 设置最小高度为默认高度，最大高度不超过200像素
                maxHeight = Math.max(row.getHeightInPoints(), Math.min(maxHeight, 200.0f));
                row.setHeightInPoints(maxHeight);
            }
        }
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
     * 工厂方法：创建工具回调
     */
    public static ToolCallback createToolCallback() {
        return FunctionToolCallback.builder("excel_write", new ExcelWriteTool())
                .description("写入数据到Excel文件。" +
                        "【重要】编辑已存在的Excel文件时，会自动保护原有内容。" +
                        "参数包括: filePath文件路径(必填), sheetName工作表名称(可选,默认Sheet1), " +
                        "headers表头列表(可选), data数据列表(可选,二维数组), " +
                        "appendMode是否追加模式(可选,默认false), " +
                        "templatePath模板文件路径(可选,提供时会保留模板所有样式), " +
                        "cellMapping单元格映射(可选,Map格式,key为单元格位置如\"A1\",value为值)。" +
                        "【推荐】编辑合同等模板文件时，使用templatePath+cellMapping组合可完美保留所有样式。" +
                        "【安全】当目标文件已存在且未提供templatePath或appendMode时，系统会自动保护原有内容。" +
                        "【注意】对于包含大量文本内容的操作，请考虑分批处理以避免JSON解析错误。")
                .inputType(WriteRequest.class)
                .build();
    }

    /**
     * 写入请求参数
     */
    public static class WriteRequest {
        @JsonProperty(required = true)
        @JsonPropertyDescription("Excel文件的完整路径")
        public String filePath;

        @JsonProperty
        @JsonPropertyDescription("工作表名称，默认Sheet1")
        public String sheetName;

        @JsonProperty
        @JsonPropertyDescription("表头列表，如[\"姓名\", \"年龄\", \"城市\"]。仅用于新建文件或覆盖模式")
        public List<String> headers;

        @JsonProperty
        @JsonPropertyDescription("数据列表，二维数组格式，如[[\"张三\", 25, \"北京\"], [\"李四\", 30, \"上海\"]]。编辑已有文件时建议使用cellMapping")
        public List<List<Object>> data;

        @JsonProperty
        @JsonPropertyDescription("是否追加模式，true为追加数据到文件末尾，false为覆盖写入（编辑已有文件时会自动保护原有内容），默认false")
        public Boolean appendMode;

        @JsonProperty
        @JsonPropertyDescription("模板文件路径，提供时会保留模板的所有样式（包括列宽、合并单元格、格式等）。编辑已有文件时可设置为原文件路径")
        public String templatePath;

        @JsonProperty
        @JsonPropertyDescription("单元格映射，Map格式，key为单元格位置如\"A1\"，value为值。推荐用于编辑已有文件，可精确控制写入位置并保留所有样式。注意：对于长文本内容，请分批处理以避免JSON解析错误")
        public Map<String, Object> cellMapping;

        @JsonProperty
        @JsonPropertyDescription("要清除区域的起始单元格（如\"A8\"），配合templatePath和cellMapping使用，清除模板原有数据")
        public String clearStartCell;

        @JsonProperty
        @JsonPropertyDescription("要清除区域的结束单元格（如\"G11\"），配合templatePath和cellMapping使用，清除模板原有数据")
        public String clearEndCell;
    }
}
