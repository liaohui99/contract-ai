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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;

/**
 * Excel分析工具
 * 用于Agent调用分析Excel文件结构信息
 * 支持.xlsx格式（Excel 2007+）
 *
 * @author Gabriel
 * @version 1.0
 */
@Slf4j
@Component
public class ExcelAnalysisTool implements BiFunction<ExcelAnalysisTool.AnalysisRequest, ToolContext, String> {

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

    /**
     * 分析Excel文件结构
     *
     * @param request 分析请求参数
     * @param context 工具上下文
     * @return 分析结果JSON字符串
     */
    @Override
    public String apply(AnalysisRequest request, ToolContext context) {
        log.info("开始分析Excel文件: {}", request.filePath);

        try {
            String filePath = request.filePath;
            String analysisType = request.analysisType != null ? request.analysisType : "full";
            String sheetName = request.sheetName;
            boolean includeStyle = request.includeStyle != null ? request.includeStyle : false;

            Map<String, Object> result = analyzeExcel(filePath, analysisType, sheetName);

            result.put("success", true);
            result.put("filePath", filePath);
            result.put("analysisType", analysisType);

            if (includeStyle) {
                Map<String, Object> styleInfo = ExcelStyleHelper.extractStyleInfo(filePath);
                result.put("styleInfo", styleInfo);
            }

            log.info("Excel文件分析成功");
            return toJson(result);

        } catch (Exception e) {
            log.error("分析Excel文件失败: {}", e.getMessage(), e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            return toJson(errorResponse);
        }
    }

    /**
     * 分析Excel文件
     *
     * @param filePath      文件路径
     * @param analysisType  分析类型：full(完整分析), structure(仅结构), data(数据统计)
     * @param targetSheet   目标工作表名称（可选）
     * @return 分析结果
     */
    private Map<String, Object> analyzeExcel(String filePath, String analysisType, String targetSheet) throws IOException {
        Map<String, Object> result = new HashMap<>();

        try (FileInputStream fis = new FileInputStream(filePath);
             Workbook workbook = new XSSFWorkbook(fis)) {

            java.io.File file = new java.io.File(filePath);
            result.put("fileSize", formatFileSize(file.length()));
            result.put("fileSizeBytes", file.length());

            List<Map<String, Object>> sheetsInfo = new ArrayList<>();
            int numberOfSheets = workbook.getNumberOfSheets();
            result.put("totalSheets", numberOfSheets);

            for (int i = 0; i < numberOfSheets; i++) {
                Sheet sheet = workbook.getSheetAt(i);
                String currentSheetName = sheet.getSheetName();

                if (targetSheet != null && !targetSheet.isEmpty() && !targetSheet.equals(currentSheetName)) {
                    continue;
                }

                Map<String, Object> sheetInfo = analyzeSheet(sheet, analysisType);
                sheetInfo.put("sheetName", currentSheetName);
                sheetInfo.put("sheetIndex", i);
                sheetsInfo.add(sheetInfo);
            }

            result.put("sheets", sheetsInfo);

            if (targetSheet != null && !targetSheet.isEmpty() && sheetsInfo.isEmpty()) {
                throw new IllegalArgumentException("找不到工作表: " + targetSheet);
            }
        }

        return result;
    }

    /**
     * 分析单个工作表
     */
    private Map<String, Object> analyzeSheet(Sheet sheet, String analysisType) {
        Map<String, Object> info = new HashMap<>();

        int firstRowNum = sheet.getFirstRowNum();
        int lastRowNum = sheet.getLastRowNum();
        int totalRows = lastRowNum - firstRowNum + 1;

        info.put("firstRowNum", firstRowNum);
        info.put("lastRowNum", lastRowNum);
        info.put("totalRows", totalRows);

        int maxCols = 0;
        Row firstRow = sheet.getRow(firstRowNum);
        if (firstRow != null) {
            maxCols = firstRow.getLastCellNum();
        }

        for (int i = firstRowNum; i <= lastRowNum; i++) {
            Row row = sheet.getRow(i);
            if (row != null && row.getLastCellNum() > maxCols) {
                maxCols = row.getLastCellNum();
            }
        }
        info.put("maxColumns", maxCols);

        if ("structure".equals(analysisType)) {
            return info;
        }

        List<String> headers = new ArrayList<>();
        if (firstRow != null) {
            for (int i = 0; i < maxCols; i++) {
                Cell cell = firstRow.getCell(i);
                headers.add(getCellValueAsString(cell));
            }
        }
        info.put("headers", headers);

        if ("full".equals(analysisType) || "data".equals(analysisType)) {
            List<Map<String, Object>> columnStats = analyzeColumnData(sheet, maxCols, firstRowNum);
            info.put("columnStats", columnStats);

            int emptyRows = 0;
            int dataRows = 0;
            for (int i = firstRowNum + 1; i <= lastRowNum; i++) {
                Row row = sheet.getRow(i);
                if (row == null || isRowEmpty(row, maxCols)) {
                    emptyRows++;
                } else {
                    dataRows++;
                }
            }
            info.put("dataRows", dataRows);
            info.put("emptyRows", emptyRows);

            Map<String, Integer> cellTypeDistribution = new HashMap<>();
            cellTypeDistribution.put("STRING", 0);
            cellTypeDistribution.put("NUMERIC", 0);
            cellTypeDistribution.put("BOOLEAN", 0);
            cellTypeDistribution.put("FORMULA", 0);
            cellTypeDistribution.put("BLANK", 0);
            cellTypeDistribution.put("DATE", 0);

            for (int i = firstRowNum; i <= lastRowNum; i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                for (int j = 0; j < maxCols; j++) {
                    Cell cell = row.getCell(j);
                    String typeName = getCellTypeName(cell);
                    cellTypeDistribution.merge(typeName, 1, Integer::sum);
                }
            }
            info.put("cellTypeDistribution", cellTypeDistribution);
        }

        return info;
    }

    /**
     * 分析列数据统计
     */
    private List<Map<String, Object>> analyzeColumnData(Sheet sheet, int maxCols, int firstRowNum) {
        List<Map<String, Object>> stats = new ArrayList<>();
        int lastRowNum = sheet.getLastRowNum();

        for (int col = 0; col < maxCols; col++) {
            Map<String, Object> colStat = new HashMap<>();
            colStat.put("columnIndex", col);

            Set<String> uniqueValues = new HashSet<>();
            int nonEmptyCount = 0;
            int stringCount = 0;
            int numericCount = 0;
            int dateCount = 0;
            int booleanCount = 0;

            Double minNumeric = null;
            Double maxNumeric = null;
            Double sumNumeric = 0.0;

            for (int row = firstRowNum + 1; row <= lastRowNum; row++) {
                Row r = sheet.getRow(row);
                if (r == null) continue;

                Cell cell = r.getCell(col);
                if (cell == null || cell.getCellType() == CellType.BLANK) {
                    continue;
                }

                nonEmptyCount++;

                CellType actualType = cell.getCellType();
                if (actualType == CellType.FORMULA) {
                    actualType = cell.getCachedFormulaResultType();
                }

                switch (actualType) {
                    case STRING:
                        stringCount++;
                        String strVal = cell.getStringCellValue();
                        if (strVal != null && strVal.length() <= 50) {
                            uniqueValues.add(strVal);
                        }
                        break;
                    case NUMERIC:
                        if (DateUtil.isCellDateFormatted(cell)) {
                            dateCount++;
                        } else {
                            numericCount++;
                            double numVal = cell.getNumericCellValue();
                            sumNumeric += numVal;
                            if (minNumeric == null || numVal < minNumeric) minNumeric = numVal;
                            if (maxNumeric == null || numVal > maxNumeric) maxNumeric = numVal;
                        }
                        break;
                    case BOOLEAN:
                        booleanCount++;
                        uniqueValues.add(String.valueOf(cell.getBooleanCellValue()));
                        break;
                    default:
                        break;
                }
            }

            colStat.put("nonEmptyCount", nonEmptyCount);
            colStat.put("uniqueValueCount", uniqueValues.size());
            colStat.put("stringCount", stringCount);
            colStat.put("numericCount", numericCount);
            colStat.put("dateCount", dateCount);
            colStat.put("booleanCount", booleanCount);

            if (numericCount > 0) {
                colStat.put("minValue", minNumeric);
                colStat.put("maxValue", maxNumeric);
                colStat.put("sumValue", sumNumeric);
                colStat.put("avgValue", sumNumeric / numericCount);
            }

            String inferredType = "UNKNOWN";
            if (nonEmptyCount > 0) {
                if (dateCount > stringCount && dateCount > numericCount) {
                    inferredType = "DATE";
                } else if (numericCount > stringCount) {
                    inferredType = "NUMERIC";
                } else if (stringCount > 0) {
                    inferredType = "STRING";
                } else if (booleanCount > 0) {
                    inferredType = "BOOLEAN";
                }
            }
            colStat.put("inferredType", inferredType);

            if (uniqueValues.size() <= 10 && uniqueValues.size() > 0) {
                colStat.put("sampleValues", new ArrayList<>(uniqueValues));
            }

            stats.add(colStat);
        }

        return stats;
    }

    /**
     * 判断行是否为空
     */
    private boolean isRowEmpty(Row row, int maxCols) {
        for (int i = 0; i < maxCols; i++) {
            Cell cell = row.getCell(i);
            if (cell != null && cell.getCellType() != CellType.BLANK) {
                return false;
            }
        }
        return true;
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
     * 格式化文件大小
     */
    private String formatFileSize(long bytes) {
        if (bytes < 1024) {
            return bytes + " B";
        } else if (bytes < 1024 * 1024) {
            return String.format("%.2f KB", bytes / 1024.0);
        } else if (bytes < 1024 * 1024 * 1024) {
            return String.format("%.2f MB", bytes / (1024.0 * 1024));
        } else {
            return String.format("%.2f GB", bytes / (1024.0 * 1024 * 1024));
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
            sb.append(valueToJson(value));
        }
        sb.append("}");
        return sb.toString();
    }

    /**
     * 值转JSON
     */
    private String valueToJson(Object value) {
        if (value == null) {
            return "null";
        } else if (value instanceof String) {
            return "\"" + escapeJson((String) value) + "\"";
        } else if (value instanceof Number || value instanceof Boolean) {
            return value.toString();
        } else if (value instanceof List) {
            return listToJson((List<?>) value);
        } else if (value instanceof Map) {
            return toJson((Map<String, Object>) value);
        } else {
            return "\"" + escapeJson(value.toString()) + "\"";
        }
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
            sb.append(valueToJson(item));
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
        return FunctionToolCallback.builder("excel_analysis", new ExcelAnalysisTool())
                .description("分析Excel文件结构信息。参数包括: filePath文件路径(必填), " +
                        "analysisType分析类型(可选: full完整分析/structure仅结构/data数据统计,默认full), " +
                        "sheetName工作表名称(可选,不填则分析所有工作表), " +
                        "includeStyle是否包含样式信息(可选,默认false)。返回文件结构、列统计、数据类型分布等信息，可选返回样式信息用于保留模板格式。")
                .inputType(AnalysisRequest.class)
                .build();
    }

    /**
     * 分析请求参数
     */
    public static class AnalysisRequest {
        @JsonProperty(required = true)
        @JsonPropertyDescription("Excel文件的完整路径")
        public String filePath;

        @JsonProperty
        @JsonPropertyDescription("分析类型: full(完整分析), structure(仅结构), data(数据统计)，默认full")
        public String analysisType;

        @JsonProperty
        @JsonPropertyDescription("目标工作表名称，不填则分析所有工作表")
        public String sheetName;

        @JsonProperty
        @JsonPropertyDescription("是否包含样式信息，默认false")
        public Boolean includeStyle;
    }
}
