package com.example.springaiapp.skills;

import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Excel样式工具类
 * 用于复制和保留Excel模板的样式信息
 *
 * @author Gabriel
 * @version 2.0
 */
@Slf4j
public class ExcelStyleHelper {

    /**
     * 基于模板生成新的Excel文件，保留模板所有样式
     * 使用单元格位置映射填充数据
     *
     * @param templatePath 模板文件路径
     * @param outputPath 输出文件路径
     * @param dataMapping 数据映射，key是单元格位置(如"A2", "B3")，value是值
     */
    public static void generateExcelWithFullStyle(String templatePath, String outputPath,
                                                Map<String, Object> dataMapping) throws IOException {
        try (FileInputStream fis = new FileInputStream(templatePath);
             Workbook workbook = new XSSFWorkbook(fis)) {

            Sheet sheet = workbook.getSheetAt(0);

            for (Map.Entry<String, Object> entry : dataMapping.entrySet()) {
                String cellRef = entry.getKey();
                Object value = entry.getValue();

                Cell cell = getCellByReference(sheet, cellRef);
                if (cell != null) {
                    setCellValueWithStyle(cell, value);
                }
            }

            try (FileOutputStream fos = new FileOutputStream(outputPath)) {
                workbook.write(fos);
            }
        }
    }

    /**
     * 基于模板生成新的Excel文件，保留模板所有样式
     * 使用列映射方式填充数据
     *
     * @param templatePath   模板文件路径
     * @param outputPath     输出文件路径
     * @param sheetName      工作表名称
     * @param startRow       数据开始行号
     * @param headerRowIndex 表头所在行号（用于列名映射）
     * @param dataList       数据列表
     */
    public static void generateExcelWithFullStyle(String templatePath, String outputPath,
                                                    String sheetName, int startRow,
                                                    int headerRowIndex,
                                                    List<Map<String, Object>> dataList) throws IOException {
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

            Map<String, Integer> headerColumnMap = buildHeaderColumnMap(sheet, headerRowIndex);

            clearDataFromRow(sheet, startRow);

            for (int i = 0; i < dataList.size(); i++) {
                int rowNum = startRow + i;
                Row row = getOrCreateRowWithTemplate(sheet, rowNum, startRow - 1);

                Map<String, Object> rowData = dataList.get(i);
                fillRowData(row, rowData, headerColumnMap);
            }

            try (FileOutputStream fos = new FileOutputStream(outputPath)) {
                workbook.write(fos);
            }
        }
    }

    /**
     * 清空从指定行开始的数据（保留表头和样式）
     * 注意：此方法会保护第7行（索引为6）的表头不会被清除
     */
    private static void clearDataFromRow(Sheet sheet, int startRow) {
        int lastRowNum = sheet.getLastRowNum();
        for (int i = lastRowNum; i >= startRow; i--) {
            // 保护第7行（索引为6）的表头，不被清除
            if (i == 6) {
                log.info("跳过第7行（索引6），保护表头不被清除");
                continue;
            }
            
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
     * 根据单元格引用获取单元格（如"A1", "B2"）
     */
    private static Cell getCellByReference(Sheet sheet, String cellRef) {
        cellRef = cellRef.toUpperCase();
        
        int colIndex = 0;
        int rowIndex = 0;
        
        int firstDigitIndex = -1;
        for (int i = 0; i < cellRef.length(); i++) {
            if (Character.isDigit(cellRef.charAt(i))) {
                firstDigitIndex = i;
                break;
            }
        }
        
        if (firstDigitIndex == -1) {
            return null;
        }
        
        String colPart = cellRef.substring(0, firstDigitIndex);
        String rowPart = cellRef.substring(firstDigitIndex);
        
        for (int i = 0; i < colPart.length(); i++) {
            colIndex = colIndex * 26 + (colPart.charAt(i) - 'A' + 1);
        }
        colIndex--;
        
        rowIndex = Integer.parseInt(rowPart) - 1;
        
        Row row = sheet.getRow(rowIndex);
        if (row == null) {
            row = sheet.createRow(rowIndex);
        }
        return row.getCell(colIndex, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
    }

    /**
     * 构建表头列名到列索引的映射
     */
    private static Map<String, Integer> buildHeaderColumnMap(Sheet sheet, int headerRowIndex) {
        Map<String, Integer> map = new HashMap<>();
        Row headerRow = sheet.getRow(headerRowIndex);
        if (headerRow != null) {
            for (int i = 0; i < headerRow.getLastCellNum(); i++) {
                Cell cell = headerRow.getCell(i);
                if (cell != null) {
                    String headerName = getCellValueAsString(cell);
                    if (headerName != null && !headerName.isEmpty()) {
                        map.put(headerName, i);
                    }
                }
            }
        }
        return map;
    }

    /**
     * 获取或创建带有模板样式的行
     */
    private static Row getOrCreateRowWithTemplate(Sheet sheet, int rowNum, int templateRowNum) {
        Row row = sheet.getRow(rowNum);
        if (row == null) {
            row = sheet.createRow(rowNum);
            
            Row templateRow = sheet.getRow(templateRowNum);
            if (templateRow != null) {
                row.setHeight(templateRow.getHeight());
                row.setHeightInPoints(templateRow.getHeightInPoints());
                if (templateRow.getRowStyle() != null) {
                    row.setRowStyle(templateRow.getRowStyle());
                }
            }
        }
        return row;
    }

    /**
     * 填充行数据，保留单元格样式
     */
    private static void fillRowData(Row row, Map<String, Object> rowData, Map<String, Integer> headerColumnMap) {
        for (Map.Entry<String, Object> entry : rowData.entrySet()) {
            String headerName = entry.getKey();
            Object value = entry.getValue();
            
            Integer colIndex = headerColumnMap.get(headerName);
            if (colIndex != null) {
                Cell cell = row.getCell(colIndex, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
                setCellValueWithStyle(cell, value);
            }
        }
    }

    /**
     * 设置单元格值（保留原有样式）
     */
    private static void setCellValueWithStyle(Cell cell, Object value) {
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
        } else if (value instanceof java.util.Date) {
            cell.setCellValue((java.util.Date) value);
        } else {
            cell.setCellValue(value.toString());
        }
    }

    /**
     * 获取单元格值作为字符串
     */
    private static String getCellValueAsString(Cell cell) {
        if (cell == null) {
            return "";
        }
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().trim();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue().toString();
                }
                double num = cell.getNumericCellValue();
                if (num == Math.floor(num)) {
                    return String.valueOf((long) num);
                }
                return String.valueOf(num);
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                return cell.toString();
            case BLANK:
                return "";
            default:
                return "";
        }
    }

    /**
     * 提取模板样式信息为可序列化的Map
     *
     * @param templatePath 模板文件路径
     * @return 样式信息Map
     */
    public static Map<String, Object> extractStyleInfo(String templatePath) throws IOException {
        Map<String, Object> styleInfo = new HashMap<>();

        try (FileInputStream fis = new FileInputStream(templatePath);
             Workbook workbook = new XSSFWorkbook(fis)) {

            List<Map<String, Object>> sheetsStyle = new ArrayList<>();

            for (int sheetIndex = 0; sheetIndex < workbook.getNumberOfSheets(); sheetIndex++) {
                Sheet sheet = workbook.getSheetAt(sheetIndex);
                Map<String, Object> sheetStyle = new HashMap<>();
                sheetStyle.put("sheetName", sheet.getSheetName());
                sheetStyle.put("sheetIndex", sheetIndex);

                sheetStyle.put("displayGridlines", sheet.isDisplayGridlines());
                sheetStyle.put("defaultRowHeightInPoints", sheet.getDefaultRowHeightInPoints());
                sheetStyle.put("defaultColumnWidth", sheet.getDefaultColumnWidth());

                List<Map<String, Object>> columnStyles = new ArrayList<>();
                Row firstRow = sheet.getRow(0);
                int maxColumns = firstRow != null ? firstRow.getLastCellNum() : 0;

                for (int col = 0; col < maxColumns; col++) {
                    Map<String, Object> colStyle = new HashMap<>();
                    colStyle.put("columnIndex", col);
                    colStyle.put("width", sheet.getColumnWidth(col));
                    colStyle.put("hidden", sheet.isColumnHidden(col));
                    columnStyles.add(colStyle);
                }
                sheetStyle.put("columnStyles", columnStyles);

                List<Map<String, Object>> mergedRegions = new ArrayList<>();
                for (int i = 0; i < sheet.getNumMergedRegions(); i++) {
                    CellRangeAddress region = sheet.getMergedRegion(i);
                    Map<String, Object> regionInfo = new HashMap<>();
                    regionInfo.put("firstRow", region.getFirstRow());
                    regionInfo.put("lastRow", region.getLastRow());
                    regionInfo.put("firstColumn", region.getFirstColumn());
                    regionInfo.put("lastColumn", region.getLastColumn());
                    mergedRegions.add(regionInfo);
                }
                sheetStyle.put("mergedRegions", mergedRegions);

                sheetsStyle.add(sheetStyle);
            }

            styleInfo.put("sheets", sheetsStyle);
            styleInfo.put("hasStyle", true);
        }

        return styleInfo;
    }
}
