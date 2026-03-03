package com.example.springaiapp.skills;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 技能测试控制器
 * 用于测试各种AI技能的功能
 */
@RestController
@RequestMapping("/skills")
public class SkillsTestController {

    @Autowired
    private PurchaseContractSkill purchaseContractSkill;

    /**
     * 测试采购合同生成技能
     */
    @PostMapping("/purchase-contract/generate")
    public String testPurchaseContractGeneration(@RequestBody Map<String, Object> requestData) {
        try {
            // 调用技能生成合同
            String result = purchaseContractSkill.apply(requestData, null);
            return result;
        } catch (Exception e) {
            return "生成合同时出错: " + e.getMessage();
        }
    }

    /**
     * 简单测试端点
     */
    @GetMapping("/test")
    public String testSkills() {
        Map<String, Object> testData = new HashMap<>();
        testData.put("contractNumber", "CG-2025-001");
        testData.put("supplierName", "示例供应商有限公司");
        testData.put("signDate", "2025-01-15");
        
        return purchaseContractSkill.apply(testData, null);
    }
}