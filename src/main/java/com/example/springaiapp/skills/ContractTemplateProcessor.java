package com.example.springaiapp.skills;

import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * 合同模板处理工具
 * 专门用于处理采购合同模板，完美保留所有样式
 *
 * @author Gabriel
 * @version 1.0
 */
@Slf4j
public class ContractTemplateProcessor {

    /**
     * 基于合同模板生成新的合同文件
     * 完美保留所有样式（包括合并单元格、列宽、格式等）
     *
     * @param templatePath 模板文件路径
     * @param outputPath   输出文件路径
     * @param data         合同数据（Map格式，key为单元格位置如"A1"，value为值）
     */
    public static void generateContract(String templatePath, String outputPath,
                                        Map<String, Object> data) throws IOException {
        generateContract(templatePath, outputPath, data, null, null);
    }

    /**
     * 基于合同模板生成新的合同文件
     * 完美保留所有样式（包括合并单元格、列宽、格式等）
     *
     * @param templatePath 模板文件路径
     * @param outputPath   输出文件路径
     * @param data         合同数据（Map格式，key为单元格位置如"A1"，value为值）
     * @param clearStartCell 要清除区域的起始单元格（如"A8"），null表示不清空
     * @param clearEndCell 要清除区域的结束单元格（如"G11"），null表示不清空
     */
    public static void generateContract(String templatePath, String outputPath,
                                        Map<String, Object> data,
                                        String clearStartCell, String clearEndCell) throws IOException {
        log.info("开始生成合同: {} -> {}", templatePath, outputPath);

        try (FileInputStream fis = new FileInputStream(templatePath);
             Workbook workbook = new XSSFWorkbook(fis)) {

            Sheet sheet = workbook.getSheetAt(0);

            if (clearStartCell != null && clearEndCell != null) {
                log.info("清除区域: {} - {}", clearStartCell, clearEndCell);
                clearCellRange(sheet, clearStartCell, clearEndCell);
            }

            int cellsUpdated = 0;
            for (Map.Entry<String, Object> entry : data.entrySet()) {
                String cellRef = entry.getKey();
                Object value = entry.getValue();

                Cell cell = getCellByReference(sheet, cellRef);
                if (cell != null) {
                    setCellValuePreserveStyle(cell, value);
                    cellsUpdated++;
                } else {
                    log.warn("无法找到或创建单元格: {}", cellRef);
                }
            }

            try (FileOutputStream fos = new FileOutputStream(outputPath)) {
                workbook.write(fos);
            }

            log.info("合同生成成功: {}, 共更新了 {} 个单元格", outputPath, cellsUpdated);
        }
    }

    /**
     * 基于合同模板生成新的合同文件（使用二维数组数据）
     * 完美保留所有样式（包括合并单元格、列宽、格式等）
     *
     * @param templatePath 模板文件路径
     * @param outputPath   输出文件路径
     * @param dataList     数据列表（二维数组格式）
     * @param startRow     数据开始行号（从0开始）
     */
    public static void generateContractWithListData(String templatePath, String outputPath,
                                                      List<List<Object>> dataList, int startRow) throws IOException {
        log.info("开始生成合同（使用列表数据）: {} -> {}", templatePath, outputPath);

        try (FileInputStream fis = new FileInputStream(templatePath);
             Workbook workbook = new XSSFWorkbook(fis)) {

            Sheet sheet = workbook.getSheetAt(0);

            for (int i = 0; i < dataList.size(); i++) {
                int rowNum = startRow + i;
                List<Object> rowData = dataList.get(i);

                if (rowData != null) {
                    Row row = getOrCreateRow(sheet, rowNum);
                    for (int j = 0; j < rowData.size(); j++) {
                        Cell cell = row.getCell(j, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
                        Object value = rowData.get(j);
                        setCellValuePreserveStyle(cell, value);
                    }
                }
            }

            try (FileOutputStream fos = new FileOutputStream(outputPath)) {
                workbook.write(fos);
            }

            log.info("合同生成成功: {}", outputPath);
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
     * 获取或创建行
     */
    private static Row getOrCreateRow(Sheet sheet, int rowNum) {
        Row row = sheet.getRow(rowNum);
        if (row == null) {
            row = sheet.createRow(rowNum);
        }
        return row;
    }

    /**
     * 清除指定区域的单元格内容，保留样式
     *
     * @param sheet 工作表
     * @param startCellRef 起始单元格引用（如"A8"）
     * @param endCellRef 结束单元格引用（如"G11"）
     */
    private static void clearCellRange(Sheet sheet, String startCellRef, String endCellRef) {
        int[] startCoords = parseCellReference(startCellRef);
        int[] endCoords = parseCellReference(endCellRef);

        if (startCoords == null || endCoords == null) {
            log.warn("无法解析单元格引用: {} - {}", startCellRef, endCellRef);
            return;
        }

        int startRow = startCoords[0];
        int startCol = startCoords[1];
        int endRow = endCoords[0];
        int endCol = endCoords[1];

        log.info("清除区域内容: {}({},{}) 到 {}({},{})", 
                 startCellRef, startRow, startCol, endCellRef, endRow, endCol);

        for (int rowNum = startRow; rowNum <= endRow; rowNum++) {
            Row row = sheet.getRow(rowNum);
            if (row == null) {
                continue;
            }
            for (int colNum = startCol; colNum <= endCol; colNum++) {
                Cell cell = row.getCell(colNum);
                if (cell != null) {
                    cell.setBlank();
                }
            }
        }
    }

    /**
     * 解析单元格引用，返回[行索引, 列索引]
     *
     * @param cellRef 单元格引用（如"A1"）
     * @return [行索引, 列索引]，解析失败返回null
     */
    private static int[] parseCellReference(String cellRef) {
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

        try {
            rowIndex = Integer.parseInt(rowPart) - 1;
        } catch (NumberFormatException e) {
            return null;
        }

        return new int[]{rowIndex, colIndex};
    }

    /**
     * 设置单元格值，完全保留原有样式
     */
    private static void setCellValuePreserveStyle(Cell cell, Object value) {
        if (cell == null) {
            log.warn("尝试设置空单元格的值");
            return;
        }
        
        if (value == null) {
            cell.setBlank();
            log.debug("单元格已清空: {}{}", 
                (char)('A' + cell.getColumnIndex()), 
                cell.getRowIndex() + 1);
            return;
        }

        try {
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
            log.debug("成功设置单元格值: {}{} = {}", 
                (char)('A' + cell.getColumnIndex()), 
                cell.getRowIndex() + 1,
                value.toString().substring(0, Math.min(50, value.toString().length())));
        } catch (Exception e) {
            log.error("设置单元格值失败: {}{} = {}, 错误: {}", 
                (char)('A' + cell.getColumnIndex()), 
                cell.getRowIndex() + 1,
                value,
                e.getMessage());
        }
    }
}