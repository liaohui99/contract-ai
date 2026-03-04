package com.example.springaiapp.skills;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.function.FunctionToolCallback;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

/**
 * 合同智能解析工具
 * 支持解析Excel(.xlsx)和Word(.docx)格式的合同文件
 * 使用LLM模型智能提取关键信息，将非结构化文档转化为结构化数据
 *
 * @author Gabriel
 * @version 2.0
 */
@Slf4j
@Component
public class ContractAnalyzerTool implements BiFunction<ContractAnalyzerTool.AnalyzeRequest, ToolContext, String> {

    private static ChatClient chatClient;
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    public void setChatModel(ChatModel chatModel) {
        if (chatClient == null && chatModel != null) {
            chatClient = ChatClient.builder(chatModel).build();
            log.info("ContractAnalyzerTool ChatClient初始化成功");
        }
    }

    private static final String EXTRACTION_PROMPT_TEMPLATE = """
            你是一个专业的合同信息提取助手。请从以下合同内容中提取关键信息，并以JSON格式返回。
            
            ## 合同内容
            ```
            %s
            ```
            
            ## 提取要求
            请提取以下信息（如果某项信息在合同中不存在，则返回null）：
            
            1. 合同编号
            2. 合同类型：如"采购合同"、"销售合同"、"服务合同"等
            3. 签订日期：格式为yyyy-MM-dd
            4. 签订地点
            5. 甲方信息：
               - 单位名称
               - 法定代表人
               - 联系电话
               - 开户银行
               - 银行账号
            6. 乙方信息：同甲方结构
            7. 总金额：数字类型
            8. 金额大写
            9. 商品明细列表：每个商品包含
               - 品名
               - 规格型号
               - 数量
               - 单位
               - 单价
               - 金额
               - 备注
            10. 条款信息：
                - 交货时间
                - 交货地点
                - 质量要求
                - 运费方式
                - 支付方式
                - 违约责任
                - 争议解决
            
            ## 返回格式
            请严格按照以下JSON格式返回，不要添加任何其他内容：
            ```json
            {
              "contractNumber": "合同编号",
              "contractType": "合同类型",
              "signDate": "签订日期",
              "signLocation": "签订地点",
              "partyA": {
                "name": "甲方名称",
                "legalRepresentative": "法定代表人",
                "phone": "电话",
                "bank": "开户行",
                "account": "账号"
              },
              "partyB": {
                "name": "乙方名称",
                "legalRepresentative": "法定代表人",
                "phone": "电话",
                "bank": "开户行",
                "account": "账号"
              },
              "totalAmount": 10000.00,
              "amountInWords": "壹万元整",
              "products": [
                {
                  "name": "商品名称",
                  "specification": "规格",
                  "quantity": 10,
                  "unit": "个",
                  "unitPrice": 100.00,
                  "amount": 1000.00,
                  "remark": "备注"
                }
              ],
              "terms": {
                "deliveryTime": "交货时间",
                "deliveryLocation": "交货地点",
                "qualityRequirement": "质量要求",
                "freightMethod": "运费方式",
                "paymentMethod": "支付方式",
                "liability": "违约责任",
                "disputeResolution": "争议解决"
              }
            }
            ```
            
            请直接返回JSON，不要包含任何解释或说明。
            """;

    /**
     * 解析合同文件
     *
     * @param request 解析请求参数
     * @param context 工具上下文
     * @return 解析结果JSON字符串
     */
    @Override
    public String apply(AnalyzeRequest request, ToolContext context) {
        log.info("开始解析合同文件: {}", request.filePath);

        try {
            String filePath = request.filePath;
            String fileType = getFileType(filePath);

            String contractContent;
            if ("xlsx".equals(fileType)) {
                contractContent = extractExcelContent(filePath);
            } else if ("docx".equals(fileType)) {
                contractContent = extractWordContent(filePath);
            } else {
                throw new IllegalArgumentException("不支持的文件格式: " + fileType + "，仅支持xlsx和docx格式");
            }

            ContractInfo contractInfo = extractWithLLM(contractContent);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("filePath", filePath);
            response.put("fileType", fileType);
            response.put("contractInfo", contractInfo);
            response.put("extractedAt", new java.text.SimpleDateFormat("yyyy-MM-dd").format(new java.util.Date()));

            log.info("合同文件解析成功: {}", filePath);
            return toJson(response);

        } catch (Exception e) {
            log.error("解析合同文件失败: {}", e.getMessage(), e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            return toJson(errorResponse);
        }
    }

    /**
     * 获取文件类型
     */
    private String getFileType(String filePath) {
        if (filePath == null) {
            throw new IllegalArgumentException("文件路径不能为空");
        }
        String lowerPath = filePath.toLowerCase();
        if (lowerPath.endsWith(".xlsx")) {
            return "xlsx";
        } else if (lowerPath.endsWith(".docx")) {
            return "docx";
        }
        throw new IllegalArgumentException("不支持的文件格式，仅支持.xlsx和.docx");
    }

    /**
     * 提取Excel合同内容为文本
     */
    private String extractExcelContent(String filePath) throws IOException {
        StringBuilder sb = new StringBuilder();

        try (FileInputStream fis = new FileInputStream(filePath);
             Workbook workbook = new XSSFWorkbook(fis)) {

            Sheet sheet = workbook.getSheetAt(0);
            int lastRowNum = sheet.getLastRowNum();

            sb.append("=== 合同内容 ===\n\n");

            for (int i = 0; i <= lastRowNum; i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                StringBuilder rowContent = new StringBuilder();
                for (int j = 0; j < row.getLastCellNum(); j++) {
                    Cell cell = row.getCell(j);
                    String cellValue = getCellValueAsString(cell);
                    if (cellValue != null && !cellValue.isEmpty()) {
                        if (rowContent.length() > 0) {
                            rowContent.append(" | ");
                        }
                        rowContent.append(cellValue);
                    }
                }

                if (rowContent.length() > 0) {
                    sb.append(rowContent).append("\n");
                }
            }
        }

        return sb.toString();
    }

    /**
     * 提取Word合同内容为文本
     */
    private String extractWordContent(String filePath) throws IOException {
        StringBuilder sb = new StringBuilder();

        try (FileInputStream fis = new FileInputStream(filePath);
             XWPFDocument document = new XWPFDocument(fis)) {

            sb.append("=== 合同内容 ===\n\n");

            for (XWPFParagraph para : document.getParagraphs()) {
                String text = para.getText();
                if (text != null && !text.trim().isEmpty()) {
                    sb.append(text).append("\n");
                }
            }

            sb.append("\n=== 表格内容 ===\n\n");
            int tableIndex = 1;
            for (XWPFTable table : document.getTables()) {
                sb.append("--- 表格 ").append(tableIndex++).append(" ---\n");
                for (XWPFTableRow row : table.getRows()) {
                    StringBuilder rowContent = new StringBuilder();
                    for (XWPFTableCell cell : row.getTableCells()) {
                        String cellText = cell.getText();
                        if (cellText != null && !cellText.trim().isEmpty()) {
                            if (rowContent.length() > 0) {
                                rowContent.append(" | ");
                            }
                            rowContent.append(cellText.trim());
                        }
                    }
                    if (rowContent.length() > 0) {
                        sb.append(rowContent).append("\n");
                    }
                }
                sb.append("\n");
            }
        }

        return sb.toString();
    }

    /**
     * 使用LLM提取合同信息
     */
    private ContractInfo extractWithLLM(String contractContent) {
        if (chatClient == null) {
            log.warn("ChatClient未初始化，使用规则提取");
            return extractWithRules(contractContent);
        }

        try {
            String prompt = String.format(EXTRACTION_PROMPT_TEMPLATE, contractContent);

            log.info("调用LLM提取合同信息...");
            String response = chatClient.prompt()
                    .user(prompt)
                    .call()
                    .content();

            log.debug("LLM响应: {}", response);

            return parseLLMResponse(response);

        } catch (Exception e) {
            log.error("LLM提取失败，回退到规则提取: {}", e.getMessage());
            return extractWithRules(contractContent);
        }
    }

    /**
     * 解析LLM响应
     */
    private ContractInfo parseLLMResponse(String response) {
        try {
            String jsonContent = extractJsonFromResponse(response);
            if (jsonContent == null) {
                throw new IllegalArgumentException("无法从响应中提取JSON");
            }

            @SuppressWarnings("unchecked")
            Map<String, Object> resultMap = objectMapper.readValue(jsonContent, Map.class);

            ContractInfo info = new ContractInfo();
            info.setContractNumber((String) resultMap.get("contractNumber"));
            info.setContractType((String) resultMap.get("contractType"));
            info.setSignDate((String) resultMap.get("signDate"));
            info.setSignLocation((String) resultMap.get("signLocation"));

            if (resultMap.containsKey("partyA")) {
                info.setPartyA(parsePartyInfo((Map<String, Object>) resultMap.get("partyA")));
            }
            if (resultMap.containsKey("partyB")) {
                info.setPartyB(parsePartyInfo((Map<String, Object>) resultMap.get("partyB")));
            }

            if (resultMap.get("totalAmount") != null) {
                info.setTotalAmount(new BigDecimal(resultMap.get("totalAmount").toString()));
            }
            info.setAmountInWords((String) resultMap.get("amountInWords"));

            if (resultMap.containsKey("products")) {
                info.setProducts(parseProducts((List<Map<String, Object>>) resultMap.get("products")));
            }

            if (resultMap.containsKey("terms")) {
                info.setTerms(parseTerms((Map<String, Object>) resultMap.get("terms")));
            }

            return info;

        } catch (Exception e) {
            log.error("解析LLM响应失败: {}", e.getMessage());
            throw new RuntimeException("解析LLM响应失败: " + e.getMessage());
        }
    }

    /**
     * 从响应中提取JSON
     */
    private String extractJsonFromResponse(String response) {
        if (response == null) return null;

        int startIndex = response.indexOf("```json");
        if (startIndex >= 0) {
            startIndex += 7;
        } else {
            startIndex = response.indexOf("```");
            if (startIndex >= 0) {
                startIndex += 3;
            } else {
                startIndex = response.indexOf("{");
            }
        }

        if (startIndex < 0) return null;

        int endIndex = response.lastIndexOf("```");
        if (endIndex < 0 || endIndex <= startIndex) {
            endIndex = response.lastIndexOf("}");
        }

        if (endIndex < startIndex) return null;

        String json = response.substring(startIndex, endIndex + 1).trim();
        if (!json.startsWith("{")) {
            json = "{" + json;
        }
        return json;
    }

    /**
     * 解析当事人信息
     */
    private PartyInfo parsePartyInfo(Map<String, Object> map) {
        if (map == null) return null;
        PartyInfo party = new PartyInfo();
        party.setName((String) map.get("name"));
        party.setLegalRepresentative((String) map.get("legalRepresentative"));
        party.setPhone((String) map.get("phone"));
        party.setBank((String) map.get("bank"));
        party.setAccount((String) map.get("account"));
        return party;
    }

    /**
     * 解析商品列表
     */
    private List<ProductInfo> parseProducts(List<Map<String, Object>> list) {
        if (list == null) return new ArrayList<>();
        List<ProductInfo> products = new ArrayList<>();
        for (Map<String, Object> item : list) {
            ProductInfo product = new ProductInfo();
            product.setName((String) item.get("name"));
            product.setSpecification((String) item.get("specification"));
            if (item.get("quantity") != null) {
                try {
                    product.setQuantity(Integer.parseInt(item.get("quantity").toString()));
                } catch (NumberFormatException e) {
                }
            }
            product.setUnit((String) item.get("unit"));
            if (item.get("unitPrice") != null) {
                try {
                    product.setUnitPrice(new BigDecimal(item.get("unitPrice").toString()));
                } catch (NumberFormatException e) {
                }
            }
            if (item.get("amount") != null) {
                try {
                    product.setAmount(new BigDecimal(item.get("amount").toString()));
                } catch (NumberFormatException e) {
                }
            }
            product.setRemark((String) item.get("remark"));
            products.add(product);
        }
        return products;
    }

    /**
     * 解析条款信息
     */
    private TermsInfo parseTerms(Map<String, Object> map) {
        if (map == null) return null;
        TermsInfo terms = new TermsInfo();
        terms.setDeliveryTime((String) map.get("deliveryTime"));
        terms.setDeliveryLocation((String) map.get("deliveryLocation"));
        terms.setQualityRequirement((String) map.get("qualityRequirement"));
        terms.setFreightMethod((String) map.get("freightMethod"));
        terms.setPaymentMethod((String) map.get("paymentMethod"));
        terms.setLiability((String) map.get("liability"));
        terms.setDisputeResolution((String) map.get("disputeResolution"));
        return terms;
    }

    /**
     * 规则提取（备用方案）
     */
    private ContractInfo extractWithRules(String content) {
        ContractInfo info = new ContractInfo();

        try {
            java.util.regex.Pattern contractNumPattern = java.util.regex.Pattern.compile(
                    "(合同编号[：:]*\\s*|[Nn][Oo][：:.]?\\s*)([A-Za-z0-9\\-年]+)");
            java.util.regex.Matcher matcher = contractNumPattern.matcher(content);
            if (matcher.find() && matcher.group(2) != null) {
                info.setContractNumber(matcher.group(2).trim());
            }
        } catch (Exception e) {
            log.debug("提取合同编号失败: {}", e.getMessage());
        }

        if (content.contains("采购合同")) {
            info.setContractType("采购合同");
        } else if (content.contains("销售合同")) {
            info.setContractType("销售合同");
        } else {
            info.setContractType("合同");
        }

        try {
            java.util.regex.Pattern datePattern = java.util.regex.Pattern.compile(
                    "(签订时间[：:]*\\s*)(\\d{4}[-/年]\\d{1,2}[-/月]\\d{1,2}日?)");
            java.util.regex.Matcher dateMatcher = datePattern.matcher(content);
            if (dateMatcher.find() && dateMatcher.group(2) != null) {
                info.setSignDate(dateMatcher.group(2).trim());
            }
        } catch (Exception e) {
            log.debug("提取签订日期失败: {}", e.getMessage());
        }

        try {
            java.util.regex.Pattern locationPattern = java.util.regex.Pattern.compile(
                    "签订地点[：:]\\s*([^\\s\\n]+)");
            java.util.regex.Matcher locationMatcher = locationPattern.matcher(content);
            if (locationMatcher.find() && locationMatcher.group(1) != null) {
                info.setSignLocation(locationMatcher.group(1).trim());
            }
        } catch (Exception e) {
            log.debug("提取签订地点失败: {}", e.getMessage());
        }

        try {
            java.util.regex.Pattern partyAPattern = java.util.regex.Pattern.compile(
                    "甲方[（(]?买方|需方[)）]?[：:]*\\s*([^\\s\\n|]+)");
            java.util.regex.Matcher partyAMatcher = partyAPattern.matcher(content);
            if (partyAMatcher.find() && partyAMatcher.group(1) != null) {
                PartyInfo partyA = new PartyInfo();
                partyA.setName(partyAMatcher.group(1).trim());
                info.setPartyA(partyA);
            }
        } catch (Exception e) {
            log.debug("提取甲方信息失败: {}", e.getMessage());
        }

        try {
            java.util.regex.Pattern partyBPattern = java.util.regex.Pattern.compile(
                    "乙方[（(]?供方|卖方[)）]?[：:]*\\s*([^\\s\\n|]+)");
            java.util.regex.Matcher partyBMatcher = partyBPattern.matcher(content);
            if (partyBMatcher.find() && partyBMatcher.group(1) != null) {
                PartyInfo partyB = new PartyInfo();
                partyB.setName(partyBMatcher.group(1).trim());
                info.setPartyB(partyB);
            }
        } catch (Exception e) {
            log.debug("提取乙方信息失败: {}", e.getMessage());
        }

        try {
            java.util.regex.Pattern amountPattern = java.util.regex.Pattern.compile(
                    "(合计|总金额)[：:]*\\s*([\\d,]+\\.?\\d*)");
            java.util.regex.Matcher amountMatcher = amountPattern.matcher(content);
            if (amountMatcher.find() && amountMatcher.group(2) != null) {
                info.setTotalAmount(new BigDecimal(amountMatcher.group(2).replace(",", "")));
            }
        } catch (Exception e) {
            log.debug("提取总金额失败: {}", e.getMessage());
        }

        try {
            java.util.regex.Pattern chineseAmountPattern = java.util.regex.Pattern.compile(
                    "[壹贰叁肆伍陆柒捌玖拾佰仟万亿元整]+");
            java.util.regex.Matcher chineseMatcher = chineseAmountPattern.matcher(content);
            if (chineseMatcher.find() && chineseMatcher.group() != null) {
                info.setAmountInWords(chineseMatcher.group());
            }
        } catch (Exception e) {
            log.debug("提取中文金额失败: {}", e.getMessage());
        }

        return info;
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
                return cell.getStringCellValue().trim();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return new java.text.SimpleDateFormat("yyyy-MM-dd").format(cell.getDateCellValue());
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
                return cell.toString().trim();
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
            sb.append(valueToJson(entry.getValue()));
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
            return objectToJson(value);
        }
    }

    /**
     * 对象转JSON
     */
    private String objectToJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            return "\"" + escapeJson(obj.toString()) + "\"";
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
        return FunctionToolCallback.builder("contract_analyzer", new ContractAnalyzerTool())
                .description("智能解析合同文档，自动识别并提取关键信息。" +
                        "支持Excel(.xlsx)和Word(.docx)格式的合同文件。" +
                        "使用AI模型智能提取合同编号、金额、签约方、日期等关键信息，返回结构化的JSON数据。" +
                        "参数: filePath合同文件路径(必填)。")
                .inputType(AnalyzeRequest.class)
                .build();
    }

    /**
     * 解析请求参数
     */
    @Data
    public static class AnalyzeRequest {
        @JsonProperty(required = true)
        @JsonPropertyDescription("合同文件的完整路径，支持.xlsx和.docx格式")
        private String filePath;
    }

    /**
     * 合同信息
     */
    @Data
    public static class ContractInfo {
        private String contractNumber;
        private String contractType;
        private String signDate;
        private String signLocation;
        private PartyInfo partyA;
        private PartyInfo partyB;
        private BigDecimal totalAmount;
        private Integer totalQuantity;
        private String amountInWords;
        private List<ProductInfo> products;
        private TermsInfo terms;
    }

    /**
     * 当事人信息
     */
    @Data
    public static class PartyInfo {
        private String name;
        private String legalRepresentative;
        private String delegate;
        private String phone;
        private String fax;
        private String bank;
        private String account;
    }

    /**
     * 商品信息
     */
    @Data
    public static class ProductInfo {
        private String contractNumber;
        private String name;
        private String specification;
        private Integer quantity;
        private String unit;
        private BigDecimal unitPrice;
        private BigDecimal amount;
        private String remark;
    }

    /**
     * 条款信息
     */
    @Data
    public static class TermsInfo {
        private String deliveryTime;
        private String deliveryLocation;
        private String qualityRequirement;
        private String freightMethod;
        private String paymentMethod;
        private String liability;
        private String disputeResolution;
    }
}
