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
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

/**
 * Excel读取工具
 * 用于Agent调用读取Excel文件内容
 * 支持.xlsx格式（Excel 2007+）
 *
 * @author Gabriel
 * @version 1.0
 */
@Slf4j
@Component
public class ExcelReadTool implements BiFunction<ExcelReadTool.ReadRequest, ToolContext, String> {

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

    /**
     * 读取Excel文件
     *
     * @param request 读取请求参数
     * @param context 工具上下文
     * @return 读取结果JSON字符串
     */
    @Override
    public String apply(ReadRequest request, ToolContext context) {
        log.info("开始读取Excel文件: {}", request.filePath);

        try {
            String filePath = request.filePath;
            String sheetName = request.sheetName;
            boolean hasHeader = request.hasHeader != null ? request.hasHeader : true;
            int startRow = request.startRow != null ? request.startRow : 0;
            int endRow = request.endRow != null ? request.endRow : -1;
            int startCol = request.startCol != null ? request.startCol : 0;
            int endCol = request.endCol != null ? request.endCol : -1;

            List<Map<String, Object>> result = readExcel(filePath, sheetName, hasHeader, startRow, endRow, startCol, endCol);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("filePath", filePath);
            response.put("totalRows", result.size());
            response.put("data", result);

            log.info("Excel文件读取成功，共{}行数据", result.size());
            return toJson(response);

        } catch (Exception e) {
            log.error("读取Excel文件失败: {}", e.getMessage(), e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            return toJson(errorResponse);
        }
    }

    /**
     * 读取Excel文件内容
     *
     * @param filePath  文件路径
     * @param sheetName 工作表名称（可选，为空则读取第一个工作表）
     * @param hasHeader 是否包含表头
     * @param startRow  起始行（0开始）
     * @param endRow    结束行（-1表示到最后一行）
     * @param startCol  起始列（0开始）
     * @param endCol    结束列（-1表示到最后一列）
     * @return 数据列表
     */
    private List<Map<String, Object>> readExcel(String filePath, String sheetName,
                                                  boolean hasHeader, int startRow, int endRow,
                                                  int startCol, int endCol) throws IOException {
        List<Map<String, Object>> dataList = new ArrayList<>();

        try (FileInputStream fis = new FileInputStream(filePath);
             Workbook workbook = new XSSFWorkbook(fis)) {

            Sheet sheet = getSheet(workbook, sheetName);
            if (sheet == null) {
                throw new IllegalArgumentException("找不到工作表: " + (sheetName != null ? sheetName : "第一个工作表"));
            }

            int firstRowNum = sheet.getFirstRowNum();
            int lastRowNum = sheet.getLastRowNum();

            if (endRow >= 0 && endRow < lastRowNum) {
                lastRowNum = endRow;
            }

            String[] headers = null;
            int dataStartRow = startRow;

            if (hasHeader && startRow <= lastRowNum) {
                Row headerRow = sheet.getRow(startRow);
                if (headerRow != null) {
                    headers = readRowAsArray(headerRow, startCol, endCol);
                }
                dataStartRow = startRow + 1;
            }

            for (int i = dataStartRow; i <= lastRowNum; i++) {
                Row row = sheet.getRow(i);
                if (row == null) {
                    continue;
                }

                Map<String, Object> rowData = new HashMap<>();
                int colStart = startCol;
                int colEnd = endCol >= 0 ? endCol : row.getLastCellNum() - 1;

                for (int j = colStart; j <= colEnd; j++) {
                    Cell cell = row.getCell(j);
                    Object value = getCellValue(cell);

                    String key = (headers != null && j < headers.length && headers[j] != null)
                            ? headers[j]
                            : "column_" + j;
                    rowData.put(key, value);
                }

                dataList.add(rowData);
            }
        }

        return dataList;
    }

    /**
     * 获取工作表
     */
    private Sheet getSheet(Workbook workbook, String sheetName) {
        if (sheetName != null && !sheetName.isEmpty()) {
            return workbook.getSheet(sheetName);
        }
        return workbook.getSheetAt(0);
    }

    /**
     * 读取行数据为数组
     */
    private String[] readRowAsArray(Row row, int startCol, int endCol) {
        int colEnd = endCol >= 0 ? endCol : row.getLastCellNum() - 1;
        int length = colEnd - startCol + 1;
        String[] values = new String[length];

        for (int i = 0; i < length; i++) {
            Cell cell = row.getCell(startCol + i);
            Object value = getCellValue(cell);
            values[i] = value != null ? value.toString() : "";
        }

        return values;
    }

    /**
     * 获取单元格值
     */
    private Object getCellValue(Cell cell) {
        if (cell == null) {
            return null;
        }

        CellType cellType = cell.getCellType();

        switch (cellType) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return DATE_FORMAT.format(cell.getDateCellValue());
                }
                double numValue = cell.getNumericCellValue();
                if (numValue == Math.floor(numValue)) {
                    return (long) numValue;
                }
                return numValue;
            case BOOLEAN:
                return cell.getBooleanCellValue();
            case FORMULA:
                return getFormulaCellValue(cell);
            case BLANK:
                return null;
            default:
                return cell.toString();
        }
    }

    /**
     * 获取公式单元格的值
     */
    private Object getFormulaCellValue(Cell cell) {
        try {
            CellType cachedType = cell.getCachedFormulaResultType();
            switch (cachedType) {
                case STRING:
                    return cell.getStringCellValue();
                case NUMERIC:
                    double numValue = cell.getNumericCellValue();
                    if (numValue == Math.floor(numValue)) {
                        return (long) numValue;
                    }
                    return numValue;
                case BOOLEAN:
                    return cell.getBooleanCellValue();
                default:
                    return cell.toString();
            }
        } catch (Exception e) {
            return cell.toString();
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
            } else if (value instanceof Map) {
                sb.append(toJson((Map<String, Object>) value));
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
            } else if (item instanceof Map) {
                sb.append(toJson((Map<String, Object>) item));
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
     * 工厂方法：创建工具回调
     */
    public static ToolCallback createToolCallback() {
        return FunctionToolCallback.builder("excel_read", new ExcelReadTool())
                .description("读取Excel文件内容。参数包括: filePath文件路径(必填), sheetName工作表名称(可选,默认第一个工作表), " +
                        "hasHeader是否有表头(可选,默认true), startRow起始行(可选,默认0), endRow结束行(可选,默认-1表示到最后), " +
                        "startCol起始列(可选,默认0), endCol结束列(可选,默认-1表示到最后)。返回JSON格式的数据。")
                .inputType(ReadRequest.class)
                .build();
    }

    /**
     * 读取请求参数
     */
    public static class ReadRequest {
        @JsonProperty(required = true)
        @JsonPropertyDescription("Excel文件的完整路径")
        public String filePath;

        @JsonProperty
        @JsonPropertyDescription("工作表名称，不填则读取第一个工作表")
        public String sheetName;

        @JsonProperty
        @JsonPropertyDescription("是否包含表头，默认true")
        public Boolean hasHeader;

        @JsonProperty
        @JsonPropertyDescription("起始行号(从0开始)，默认0")
        public Integer startRow;

        @JsonProperty
        @JsonPropertyDescription("结束行号，-1表示读取到最后一行，默认-1")
        public Integer endRow;

        @JsonProperty
        @JsonPropertyDescription("起始列号(从0开始)，默认0")
        public Integer startCol;

        @JsonProperty
        @JsonPropertyDescription("结束列号，-1表示读取到最后一列，默认-1")
        public Integer endCol;
    }
}
