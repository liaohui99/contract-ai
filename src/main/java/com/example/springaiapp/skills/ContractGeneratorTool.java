package com.example.springaiapp.skills;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.function.FunctionToolCallback;
import org.springframework.stereotype.Component;

import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

/**
 * Excel合同生成工具
 * 完全根据template-parser skill定义的格式和样式从零创建采购合同Excel文件
 * 不依赖任何模版文件
 *
 * @author Gabriel
 * @version 2.0
 */
@Slf4j
@Component
public class ContractGeneratorTool implements BiFunction<ContractGeneratorTool.ContractRequest, ToolContext, String> {

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
    private static final SimpleDateFormat DATE_FORMAT_CN = new SimpleDateFormat("yyyy年MM月dd日");

    private static final int[] COLUMN_WIDTHS = {3584, 5888, 7168, 2560, 2048, 2560, 3840, 4096};

    private Workbook workbook;
    private Sheet sheet;
    private CellStyle titleStyle;
    private CellStyle headerStyle;
    private CellStyle dataStyle;
    private CellStyle clauseStyle;
    private CellStyle centerStyle;

    /**
     * 生成采购合同Excel文件
     *
     * @param request 合同请求参数
     * @param context 工具上下文
     * @return 生成结果JSON字符串
     */
    @Override
    public String apply(ContractRequest request, ToolContext context) {
        log.info("开始生成采购合同: {}", request.getOutputPath());

        try {
            validateRequest(request);

            createOutputDirectory(request.getOutputPath());

            workbook = new XSSFWorkbook();
            initializeStyles();

            sheet = workbook.createSheet("采购合同");
            setupColumnWidths();

            createHeaderSection(request);
            createBasicInfoSection(request);
            createProductTableSection(request);
            createClauseSection(request);
            createSignatureSection(request);

            saveWorkbook(request.getOutputPath());

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("outputPath", request.getOutputPath());
            response.put("contractNumber", request.getContractNumber());
            response.put("productCount", request.getProducts().size());
            response.put("totalAmount", calculateTotalAmount(request.getProducts()));
            response.put("message", "采购合同生成成功");

            log.info("采购合同生成成功: {}", request.getOutputPath());
            return toJson(response);

        } catch (Exception e) {
            log.error("生成采购合同失败: {}", e.getMessage(), e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            return toJson(errorResponse);
        } finally {
            if (workbook != null) {
                try {
                    workbook.close();
                } catch (IOException e) {
                    log.error("关闭工作簿失败", e);
                }
            }
        }
    }

    /**
     * 初始化所有样式
     */
    private void initializeStyles() {
        Font titleFont = workbook.createFont();
        titleFont.setFontName("宋体");
        titleFont.setFontHeightInPoints((short) 16);
        titleFont.setBold(true);

        titleStyle = workbook.createCellStyle();
        titleStyle.setFont(titleFont);
        titleStyle.setAlignment(HorizontalAlignment.CENTER);
        titleStyle.setVerticalAlignment(VerticalAlignment.CENTER);

        Font headerFont = workbook.createFont();
        headerFont.setFontName("宋体");
        headerFont.setFontHeightInPoints((short) 11);
        headerFont.setBold(true);

        headerStyle = workbook.createCellStyle();
        headerStyle.setFont(headerFont);
        headerStyle.setAlignment(HorizontalAlignment.CENTER);
        headerStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        headerStyle.setBorderTop(BorderStyle.THIN);
        headerStyle.setBorderBottom(BorderStyle.THIN);
        headerStyle.setBorderLeft(BorderStyle.THIN);
        headerStyle.setBorderRight(BorderStyle.THIN);
        headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        Font dataFont = workbook.createFont();
        dataFont.setFontName("宋体");
        dataFont.setFontHeightInPoints((short) 10);

        dataStyle = workbook.createCellStyle();
        dataStyle.setFont(dataFont);
        dataStyle.setAlignment(HorizontalAlignment.CENTER);
        dataStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        dataStyle.setBorderTop(BorderStyle.THIN);
        dataStyle.setBorderBottom(BorderStyle.THIN);
        dataStyle.setBorderLeft(BorderStyle.THIN);
        dataStyle.setBorderRight(BorderStyle.THIN);

        Font clauseFont = workbook.createFont();
        clauseFont.setFontName("宋体");
        clauseFont.setFontHeightInPoints((short) 10);

        clauseStyle = workbook.createCellStyle();
        clauseStyle.setFont(clauseFont);
        clauseStyle.setAlignment(HorizontalAlignment.LEFT);
        clauseStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        clauseStyle.setWrapText(true);

        centerStyle = workbook.createCellStyle();
        centerStyle.setFont(dataFont);
        centerStyle.setAlignment(HorizontalAlignment.CENTER);
        centerStyle.setVerticalAlignment(VerticalAlignment.CENTER);
    }

    /**
     * 设置列宽
     */
    private void setupColumnWidths() {
        for (int i = 0; i < COLUMN_WIDTHS.length; i++) {
            sheet.setColumnWidth(i, COLUMN_WIDTHS[i]);
        }
    }

    /**
     * 创建表头区域（第1-2行）
     */
    private void createHeaderSection(ContractRequest request) {
        Row row0 = sheet.createRow(0);
        row0.setHeightInPoints(25);
        Cell cell0 = row0.createCell(0);
        cell0.setCellValue(request.getPartyA().getName());
        cell0.setCellStyle(titleStyle);
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 7));

        Row row1 = sheet.createRow(1);
        row1.setHeightInPoints(22);
        Cell cell1 = row1.createCell(0);
        cell1.setCellValue("采购合同");
        cell1.setCellStyle(titleStyle);
        sheet.addMergedRegion(new CellRangeAddress(1, 1, 0, 7));
    }

    /**
     * 创建基本信息区域（第3-5行）
     */
    private void createBasicInfoSection(ContractRequest request) {
        Row row2 = sheet.createRow(2);
        row2.setHeightInPoints(19);
        createCell(row2, 0, "甲方（买方）：", centerStyle);
        Cell partyACell = createCell(row2, 1, request.getPartyA().getName(), centerStyle);
        sheet.addMergedRegion(new CellRangeAddress(2, 2, 1, 3));
        createCell(row2, 4, "合同编号：", centerStyle);
        sheet.addMergedRegion(new CellRangeAddress(2, 2, 4, 5));
        createCell(row2, 6, request.getContractNumber(), centerStyle);
        sheet.addMergedRegion(new CellRangeAddress(2, 2, 6, 7));

        Row row3 = sheet.createRow(3);
        row3.setHeightInPoints(19);
        createCell(row3, 4, "签订地点：", centerStyle);
        sheet.addMergedRegion(new CellRangeAddress(3, 3, 4, 5));
        String signLocation = request.getSignLocation() != null ? request.getSignLocation() : "湖南醴陵";
        createCell(row3, 6, signLocation, centerStyle);
        sheet.addMergedRegion(new CellRangeAddress(3, 3, 6, 7));

        Row row4 = sheet.createRow(4);
        row4.setHeightInPoints(19);
        createCell(row4, 0, "乙方（供方）：", centerStyle);
        Cell partyBCell = createCell(row4, 1, request.getPartyB().getName(), centerStyle);
        sheet.addMergedRegion(new CellRangeAddress(4, 4, 1, 3));
        createCell(row4, 4, "签订时间：", centerStyle);
        sheet.addMergedRegion(new CellRangeAddress(4, 4, 4, 5));
        String signDate = request.getSignDate() != null ? DATE_FORMAT.format(request.getSignDate()) : "";
        createCell(row4, 6, signDate, centerStyle);
        sheet.addMergedRegion(new CellRangeAddress(4, 4, 6, 7));
    }

    /**
     * 创建商品明细表区域（第6-13行）
     */
    private void createProductTableSection(ContractRequest request) {
        Row row5 = sheet.createRow(5);
        row5.setHeightInPoints(19);
        Cell titleCell = createCell(row5, 0, "一、购销物品项及数量价款：", clauseStyle);
        sheet.addMergedRegion(new CellRangeAddress(5, 5, 0, 7));

        Row row6 = sheet.createRow(6);
        row6.setHeightInPoints(19);
        String[] headers = {"甲方销售合同号", "品名", "规格型号", "数量", "单位", "单价", "含税金额", "备注"};
        for (int i = 0; i < headers.length; i++) {
            createCell(row6, i, headers[i], headerStyle);
        }

        List<Product> products = request.getProducts();
        int startRow = 7;
        for (int i = 0; i < products.size(); i++) {
            Row row = sheet.createRow(startRow + i);
            row.setHeightInPoints(19);
            Product product = products.get(i);
            createCell(row, 0, product.getContractNumber(), dataStyle);
            createCell(row, 1, product.getName(), dataStyle);
            createCell(row, 2, product.getSpecification(), dataStyle);
            createCell(row, 3, product.getQuantity(), dataStyle);
            createCell(row, 4, product.getUnit(), dataStyle);
            createCell(row, 5, product.getUnitPrice() != null ? product.getUnitPrice().doubleValue() : null, dataStyle);
            createCell(row, 6, product.getAmount().doubleValue(), dataStyle);
            createCell(row, 7, product.getRemark(), dataStyle);
        }

        int totalRow = startRow + products.size();
        Row sumRow = sheet.createRow(totalRow);
        sumRow.setHeightInPoints(19);
        // 为合计行的所有单元格应用dataStyle，确保表格样式一致
        for (int i = 0; i < 8; i++) {
            createCell(sumRow, i, "", dataStyle);
        }
        // 设置合计行的内容
        sumRow.getCell(0).setCellValue("合计");
        int totalQuantity = products.stream().mapToInt(p -> p.getQuantity() != null ? p.getQuantity() : 0).sum();
        sumRow.getCell(3).setCellValue(totalQuantity);
        BigDecimal totalAmount = calculateTotalAmount(products);
        sumRow.getCell(6).setCellValue(totalAmount.doubleValue());

        Row amountRow = sheet.createRow(totalRow + 1);
        amountRow.setHeightInPoints(19);
        createCell(amountRow, 0, "（人民币）", centerStyle);
        Cell amountTextCell = createCell(amountRow, 1, convertToChineseAmount(totalAmount), centerStyle);
        sheet.addMergedRegion(new CellRangeAddress(totalRow + 1, totalRow + 1, 1, 7));
    }

    /**
     * 创建条款区域
     */
    private void createClauseSection(ContractRequest request) {
        int clauseStartRow = 7 + request.getProducts().size() + 2;

        String deliveryDateStr = "";
        if (request.getDeliveryDate() != null) {
            deliveryDateStr = DATE_FORMAT_CN.format(request.getDeliveryDate());
            if (request.getDeliveryDays() != null && request.getDeliveryDays() > 0) {
                deliveryDateStr += "（另加货运" + request.getDeliveryDays() + "天）";
            }
        }
        String[] clauses = {
            "二、交货时间： " + deliveryDateStr,
            "三、交货地点 ： " + (request.getDeliveryAddress() != null ? request.getDeliveryAddress() : "") + 
                (request.getDeliveryContact() != null ? "  " + request.getDeliveryContact() : ""),
            "四、技术标准、质量要求：",
            "五、运费方式及费用承担：乙方负责",
            "六、结算及支付方式：先付款后发货（乙方提供1%增值税普票）",
            "七、违约责任：乙方必须按合同要求时间交货，如未按期交货，予以赔偿延期所致甲方的损失",
            "八、本合同未尽事宜由双方协商解决，必要时以合同附件形式另行签订。",
            "九、在本合同履行过程中，如果发生争议由双方协商解决，协商不成，则双方均可向甲方所在地人民法院提起诉讼。",
            "十、本合同一式贰份，双方各执一份。双方签字盖章后，合同生效。"
        };

        for (int i = 0; i < clauses.length; i++) {
            Row row = sheet.createRow(clauseStartRow + i);
            row.setHeightInPoints(19);
            Cell cell = createCell(row, 0, clauses[i], clauseStyle);
            sheet.addMergedRegion(new CellRangeAddress(clauseStartRow + i, clauseStartRow + i, 0, 7));
        }
    }

    /**
     * 创建签章区域
     */
    private void createSignatureSection(ContractRequest request) {
        int signStartRow = 7 + request.getProducts().size() + 2 + 9 + 1;

        Row row0 = sheet.createRow(signStartRow);
        row0.setHeightInPoints(19);
        Cell buyerCell = createCell(row0, 0, "购货方", centerStyle);
        sheet.addMergedRegion(new CellRangeAddress(signStartRow, signStartRow, 0, 1));
        Cell notaryCell = createCell(row0, 2, "签（公）证意见：", centerStyle);
        sheet.addMergedRegion(new CellRangeAddress(signStartRow, signStartRow, 2, 4));
        Cell sellerCell = createCell(row0, 5, "供货方", centerStyle);
        sheet.addMergedRegion(new CellRangeAddress(signStartRow, signStartRow, 5, 7));

        Row row1 = sheet.createRow(signStartRow + 1);
        row1.setHeightInPoints(19);
        createCell(row1, 0, "单位名称（盖章）：" + request.getPartyA().getName(), centerStyle);
        sheet.addMergedRegion(new CellRangeAddress(signStartRow + 1, signStartRow + 1, 0, 1));
        createCell(row1, 2, "经办人：", centerStyle);
        sheet.addMergedRegion(new CellRangeAddress(signStartRow + 1, signStartRow + 1, 2, 4));
        createCell(row1, 5, "单位名称（盖章）：" + request.getPartyB().getName(), centerStyle);
        sheet.addMergedRegion(new CellRangeAddress(signStartRow + 1, signStartRow + 1, 5, 7));

        Row row2 = sheet.createRow(signStartRow + 2);
        row2.setHeightInPoints(19);
        createCell(row2, 0, "法定代表人：", centerStyle);
        sheet.addMergedRegion(new CellRangeAddress(signStartRow + 2, signStartRow + 2, 0, 1));
        String legalRepB = request.getPartyB().getLegalRepresentative() != null ? 
            "法定代表人：" + request.getPartyB().getLegalRepresentative() : "法定代表人：";
        createCell(row2, 5, legalRepB, centerStyle);
        sheet.addMergedRegion(new CellRangeAddress(signStartRow + 2, signStartRow + 2, 5, 7));

        Row row3 = sheet.createRow(signStartRow + 3);
        row3.setHeightInPoints(19);
        createCell(row3, 0, "委托代表人：", centerStyle);
        sheet.addMergedRegion(new CellRangeAddress(signStartRow + 3, signStartRow + 3, 0, 1));
        String delegateB = request.getPartyB().getDelegate() != null ? 
            "委托代表人：" + request.getPartyB().getDelegate() : "委托代表人：";
        createCell(row3, 5, delegateB, centerStyle);
        sheet.addMergedRegion(new CellRangeAddress(signStartRow + 3, signStartRow + 3, 5, 7));

        Row row4 = sheet.createRow(signStartRow + 4);
        row4.setHeightInPoints(19);
        String phoneA = request.getPartyA().getPhone() != null ? "电    话：" + request.getPartyA().getPhone() : "电    话：";
        createCell(row4, 0, phoneA, centerStyle);
        sheet.addMergedRegion(new CellRangeAddress(signStartRow + 4, signStartRow + 4, 0, 1));
        createCell(row4, 2, "签（公）证机关（章）", centerStyle);
        sheet.addMergedRegion(new CellRangeAddress(signStartRow + 4, signStartRow + 4, 2, 4));
        String phoneB = request.getPartyB().getPhone() != null ? "电    话：" + request.getPartyB().getPhone() : "电    话：";
        createCell(row4, 5, phoneB, centerStyle);
        sheet.addMergedRegion(new CellRangeAddress(signStartRow + 4, signStartRow + 4, 5, 7));

        Row row5 = sheet.createRow(signStartRow + 5);
        row5.setHeightInPoints(19);
        String faxA = request.getPartyA().getFax() != null ? "传    真：" + request.getPartyA().getFax() : "传    真：";
        createCell(row5, 0, faxA, centerStyle);
        sheet.addMergedRegion(new CellRangeAddress(signStartRow + 5, signStartRow + 5, 0, 1));
        String faxB = request.getPartyB().getFax() != null ? "传    真：" + request.getPartyB().getFax() : "传    真：";
        createCell(row5, 5, faxB, centerStyle);
        sheet.addMergedRegion(new CellRangeAddress(signStartRow + 5, signStartRow + 5, 5, 7));

        Row row6 = sheet.createRow(signStartRow + 6);
        row6.setHeightInPoints(19);
        String bankA = request.getPartyA().getBank() != null ? "开户行：" + request.getPartyA().getBank() : "开户行：";
        createCell(row6, 0, bankA, centerStyle);
        sheet.addMergedRegion(new CellRangeAddress(signStartRow + 6, signStartRow + 6, 0, 1));
        createCell(row6, 2, "         年    月    日", centerStyle);
        sheet.addMergedRegion(new CellRangeAddress(signStartRow + 6, signStartRow + 6, 2, 4));
        String bankB = request.getPartyB().getBank() != null ? "开户行：" + request.getPartyB().getBank() : "开户行：";
        createCell(row6, 5, bankB, centerStyle);
        sheet.addMergedRegion(new CellRangeAddress(signStartRow + 6, signStartRow + 6, 5, 7));

        Row row7 = sheet.createRow(signStartRow + 7);
        row7.setHeightInPoints(19);
        String accountA = request.getPartyA().getAccount() != null ? "账    号：" + request.getPartyA().getAccount() : "账    号：";
        createCell(row7, 0, accountA, centerStyle);
        sheet.addMergedRegion(new CellRangeAddress(signStartRow + 7, signStartRow + 7, 0, 1));
        createCell(row7, 2, "注：除国家另有规定外，签（公）证实行自愿原则", centerStyle);
        sheet.addMergedRegion(new CellRangeAddress(signStartRow + 7, signStartRow + 7, 2, 4));
        String accountB = request.getPartyB().getAccount() != null ? "账    号：" + request.getPartyB().getAccount() : "账    号：";
        createCell(row7, 5, accountB, centerStyle);
        sheet.addMergedRegion(new CellRangeAddress(signStartRow + 7, signStartRow + 7, 5, 7));
    }

    /**
     * 创建单元格并设置值
     */
    private Cell createCell(Row row, int columnIndex, Object value, CellStyle style) {
        Cell cell = row.createCell(columnIndex);
        if (value == null) {
            cell.setCellValue("");
        } else if (value instanceof String) {
            cell.setCellValue((String) value);
        } else if (value instanceof Number) {
            cell.setCellValue(((Number) value).doubleValue());
        } else {
            cell.setCellValue(value.toString());
        }
        cell.setCellStyle(style);
        return cell;
    }

    /**
     * 创建输出目录
     */
    private void createOutputDirectory(String outputPath) {
        java.io.File outputFile = new java.io.File(outputPath);
        java.io.File parentDir = outputFile.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            parentDir.mkdirs();
            log.info("创建输出目录: {}", parentDir.getAbsolutePath());
        }
    }

    /**
     * 保存工作簿
     */
    private void saveWorkbook(String outputPath) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(outputPath)) {
            workbook.write(fos);
        }
    }

    /**
     * 验证请求参数
     */
    private void validateRequest(ContractRequest request) {
        if (request.getOutputPath() == null || request.getOutputPath().trim().isEmpty()) {
            throw new IllegalArgumentException("输出文件路径不能为空");
        }
        if (request.getContractNumber() == null || request.getContractNumber().trim().isEmpty()) {
            throw new IllegalArgumentException("合同编号不能为空");
        }
        if (request.getPartyA() == null) {
            throw new IllegalArgumentException("甲方信息不能为空");
        }
        if (request.getPartyB() == null) {
            throw new IllegalArgumentException("乙方信息不能为空");
        }
        if (request.getProducts() == null || request.getProducts().isEmpty()) {
            throw new IllegalArgumentException("商品明细不能为空");
        }
    }

    /**
     * 计算总金额
     */
    private BigDecimal calculateTotalAmount(List<Product> products) {
        return products.stream()
                .map(p -> p.getAmount() != null ? p.getAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * 将数字金额转换为中文大写
     */
    private String convertToChineseAmount(BigDecimal amount) {
        String[] chineseNumbers = {"零", "壹", "贰", "叁", "肆", "伍", "陆", "柒", "捌", "玖"};
        String[] chineseUnits = {"", "拾", "佰", "仟"};
        String[] chineseBigUnits = {"", "万", "亿"};

        long amountLong = amount.longValue();
        if (amountLong == 0) {
            return "零元整";
        }

        StringBuilder result = new StringBuilder();
        int unitIndex = 0;

        while (amountLong > 0) {
            int section = (int) (amountLong % 10000);
            if (section > 0) {
                StringBuilder sectionStr = new StringBuilder();
                int pos = 0;
                int temp = section;

                while (temp > 0) {
                    int digit = temp % 10;
                    if (digit > 0) {
                        sectionStr.insert(0, chineseNumbers[digit] + chineseUnits[pos]);
                    } else if (sectionStr.length() > 0 && sectionStr.charAt(0) != '零') {
                        sectionStr.insert(0, "零");
                    }
                    temp /= 10;
                    pos++;
                }

                result.insert(0, sectionStr.toString() + chineseBigUnits[unitIndex]);
            }

            amountLong /= 10000;
            unitIndex++;
        }

        return result.toString() + "元整";
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
        return FunctionToolCallback.builder("contract_generator", new ContractGeneratorTool())
                .description("生成采购合同Excel文件。完全根据template-parser skill定义的格式和样式从零创建，" +
                        "不依赖模版文件。自动填充合同信息、商品明细，并计算合计金额。支持金额大写转换。")
                .inputType(ContractRequest.class)
                .build();
    }

    /**
     * 合同请求参数
     */
    @Data
    public static class ContractRequest {
        @JsonProperty(required = true)
        @JsonPropertyDescription("输出文件路径，如: output/合同-HT-2025-001.xlsx")
        private String outputPath;

        @JsonProperty(required = true)
        @JsonPropertyDescription("合同编号，如: HT-2025-001")
        private String contractNumber;

        @JsonProperty(required = true)
        @JsonPropertyDescription("甲方（买方）信息")
        private PartyInfo partyA;

        @JsonProperty(required = true)
        @JsonPropertyDescription("乙方（供方）信息")
        private PartyInfo partyB;

        @JsonProperty(required = true)
        @JsonPropertyDescription("商品明细列表")
        private List<Product> products;

        @JsonProperty
        @JsonPropertyDescription("签订地点，默认: 湖南醴陵")
        private String signLocation;

        @JsonProperty
        @JsonPropertyDescription("签订时间")
        private java.util.Date signDate;

        @JsonProperty
        @JsonPropertyDescription("交货时间")
        private java.util.Date deliveryDate;

        @JsonProperty
        @JsonPropertyDescription("货运天数")
        private Integer deliveryDays;

        @JsonProperty
        @JsonPropertyDescription("交货地点")
        private String deliveryAddress;

        @JsonProperty
        @JsonPropertyDescription("交货联系电话")
        private String deliveryContact;
    }

    /**
     * 当事人信息
     */
    @Data
    public static class PartyInfo {
        @JsonProperty(required = true)
        @JsonPropertyDescription("单位名称")
        private String name;

        @JsonProperty
        @JsonPropertyDescription("法定代表人")
        private String legalRepresentative;

        @JsonProperty
        @JsonPropertyDescription("委托代表人")
        private String delegate;

        @JsonProperty
        @JsonPropertyDescription("联系电话")
        private String phone;

        @JsonProperty
        @JsonPropertyDescription("传真号码")
        private String fax;

        @JsonProperty
        @JsonPropertyDescription("开户银行")
        private String bank;

        @JsonProperty
        @JsonPropertyDescription("银行账号")
        private String account;
    }

    /**
     * 商品信息
     */
    @Data
    public static class Product {
        @JsonProperty
        @JsonPropertyDescription("甲方销售合同号")
        private String contractNumber;

        @JsonProperty(required = true)
        @JsonPropertyDescription("品名")
        private String name;

        @JsonProperty(required = true)
        @JsonPropertyDescription("规格型号")
        private String specification;

        @JsonProperty(required = true)
        @JsonPropertyDescription("数量")
        private Integer quantity;

        @JsonProperty(required = true)
        @JsonPropertyDescription("单位")
        private String unit;

        @JsonProperty(required = true)
        @JsonPropertyDescription("单价")
        private BigDecimal unitPrice;

        @JsonProperty
        @JsonPropertyDescription("含税金额（自动计算：数量×单价）")
        private BigDecimal amount;

        @JsonProperty
        @JsonPropertyDescription("备注")
        private String remark;

        /**
         * 计算含税金额
         */
        public BigDecimal getAmount() {
            if (amount != null) {
                return amount;
            }
            if (quantity != null && unitPrice != null) {
                return unitPrice.multiply(new BigDecimal(quantity));
            }
            return BigDecimal.ZERO;
        }
    }
}
