package com.example.springaiapp;

import com.example.springaiapp.skills.ContractTemplateProcessor;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.HashMap;
import java.util.Map;

/**
 * 合同模板生成测试 - 清除模板数据
 * 验证清除模板原有数据后再填充新数据
 */
@SpringBootTest
class ContractClearTemplateDataTest {

    @Test
    void testGenerateContractWithClearTemplateData() {
        try {
            String templatePath = "src/main/resources/hetong/template.xlsx";
            String outputPath = "target/contract_with_clear_template_data.xlsx";

            Map<String, Object> contractData = new HashMap<>();
            
            contractData.put("B3", "湖南陶润会文化传播有限公司工会委员会");
            contractData.put("F2", "CG-2025-002");
            contractData.put("G3", "湖南醴陵");
            contractData.put("B4", "测试供应商有限公司");
            contractData.put("F5", "2025-03-03");
            contractData.put("B8", "测试产品1");
            contractData.put("C8", "规格型号A");
            contractData.put("D8", 10);
            contractData.put("E8", "个");
            contractData.put("F8", 100.00);
            contractData.put("G8", 1000.00);
            contractData.put("B9", "测试产品2");
            contractData.put("C9", "规格型号B");
            contractData.put("D9", 5);
            contractData.put("E9", "台");
            contractData.put("F9", 200.00);
            contractData.put("G9", 1000.00);
            contractData.put("D12", 15);
            contractData.put("G12", 2000.00);
            contractData.put("B13", "贰仟元整");
            contractData.put("A14", "二、 交货时间： 2025年3月3日");
            contractData.put("A15", "三、 交货地点： 测试地点");
            contractData.put("A16", "四、 技术标准、质量要求：符合标准");
            contractData.put("A17", "五、运费方式及费用承担：甲方承担");
            contractData.put("A18", "六、 结算及支付方式：货到付款");
            contractData.put("A19", "七、违约责任：按合同执行");
            contractData.put("B25", "单位名称（盖章）：湖南陶润会文化传播有限公司工会委员会");
            contractData.put("B28", "电    话：0731-23676922");
            contractData.put("B30", "开户行：醴陵农村商业银行城区支行");
            contractData.put("B31", "账    号：8201 0750 0025 12544");
            contractData.put("D25", "单位名称（盖章）：测试供应商有限公司");
            contractData.put("D26", "法定代表人：测试人");
            contractData.put("D28", "电    话：13800138000");
            contractData.put("D30", "开户行：测试银行");
            contractData.put("D31", "账    号：6222021234567890");

            ContractTemplateProcessor.generateContract(templatePath, outputPath, contractData, 
                    "B8", "G11");

            System.out.println("合同生成成功: " + outputPath);
            System.out.println("已清除模板原有数据（B8-G11），填充了新数据！");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
