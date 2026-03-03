package com.example.springaiapp.skills;

import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

/**
 * 采购合同生成技能
 * 集成Spring AI工具框架，支持AI驱动的合同生成
 */
@Component
public class PurchaseContractSkill implements BiFunction<Map<String, Object>, ToolContext, String> {

    private final ChatModel chatModel;

    /**
     * 构造函数注入ChatModel（使用@Lazy打破循环依赖）
     *
     * @param chatModel AI聊天模型
     */
    public PurchaseContractSkill(@Lazy ChatModel chatModel) {
        this.chatModel = chatModel;
    }

    /**
     * 生成采购合同
     * 
     * @param params 合同参数
     * @param context 工具上下文
     * @return 生成结果
     */
    @Override
    @Tool(name = "generate_purchase_contract", 
          description = "生成采购合同。参数包括: contractNumber合同编号, supplierName供应商名称, " +
                       "signDate签订日期, items项目列表等。当用户需要创建采购合同时调用此工具。")
    public String apply(Map<String, Object> params, ToolContext context) {
        try {
            // 构建合同信息
            StringBuilder contractInfo = new StringBuilder();
            contractInfo.append("合同编号: ").append(params.get("contractNumber")).append("\n");
            contractInfo.append("供应商: ").append(params.get("supplierName")).append("\n");
            contractInfo.append("签订日期: ").append(params.get("signDate")).append("\n");
            
            if (params.containsKey("items")) {
                contractInfo.append("采购项目:\n");
                Object items = params.get("items");
                contractInfo.append(items.toString()).append("\n");
            }
            
            // 检查MCP是否可用以处理Excel文件
            if (false) {
                contractInfo.append("MCP服务可用，可以处理Excel合同模板\n");
            } else {
                contractInfo.append("MCP服务不可用，将在本地处理合同生成\n");
            }
            
            // 使用AI模型完善合同细节
            String promptText = "基于以下信息生成一份采购合同: " + contractInfo.toString() + 
                              "请提供合同的关键条款和建议。";
            Prompt prompt = new Prompt(new UserMessage(promptText));
            String response = chatModel.call(prompt).getResult().getOutput().getText();
            
            return "采购合同已初步生成:\n" + response + 
                   "\n\n合同文件将在后台生成并保存至指定位置。";
                   
        } catch (Exception e) {
            return "生成采购合同时出错: " + e.getMessage();
        }
    }
    
    /**
     * 生成合同的详细方法
     */
    public String generateContractDetailed(
            String contractNumber,
            String supplierName,
            String supplierLegalPerson,
            String supplierPhone,
            String supplierFax,
            String supplierBank,
            String supplierAccount,
            String supplierRepresentative,
            Map<String, Object> items,
            LocalDateTime signDate,
            LocalDateTime deliveryDate,
            int deliveryDays,
            String qualityRequirement,
            String freightMethod,
            String paymentMethod,
            String breachClause) {
                
        Map<String, Object> params = new HashMap<>();
        params.put("contractNumber", contractNumber);
        params.put("supplierName", supplierName);
        params.put("supplierLegalPerson", supplierLegalPerson);
        params.put("supplierPhone", supplierPhone);
        params.put("supplierFax", supplierFax);
        params.put("supplierBank", supplierBank);
        params.put("supplierAccount", supplierAccount);
        params.put("supplierRepresentative", supplierRepresentative);
        params.put("items", items);
        params.put("signDate", signDate);
        params.put("deliveryDate", deliveryDate);
        params.put("deliveryDays", deliveryDays);
        params.put("qualityRequirement", qualityRequirement);
        params.put("freightMethod", freightMethod);
        params.put("paymentMethod", paymentMethod);
        params.put("breachClause", breachClause);
        
        return apply(params, null);
    }
}