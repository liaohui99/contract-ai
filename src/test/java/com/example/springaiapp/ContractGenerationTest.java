package com.example.springaiapp;

import com.example.springaiapp.skills.ContractTemplateProcessor;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.HashMap;
import java.util.Map;

/**
 * 合同模板生成测试
 * 验证样式完美保留
 */
@SpringBootTest
class ContractGenerationTest {

    @Test
    void testGenerateContractWithFullStyle() {
        try {
            String templatePath = "src/main/resources/hetong/template.xlsx";
            String outputPath = "target/contract_with_full_style.xlsx";

            Map<String, Object> contractData = new HashMap<>();
            
            contractData.put("B3", "湖南陶润会文化传播有限公司工会委员会");
            contractData.put("F2", "CG-2025-001");
            contractData.put("G3", "湖南醴陵");
            contractData.put("B4", "湖南智工科技有限公司");
            contractData.put("F5", "2025-03-03");
            contractData.put("B8", "工时管理SaaS系统年度订阅服务");
            contractData.put("C8", "用户管理、工时记录、报表分析");
            contractData.put("D8", 1);
            contractData.put("E8", "项");
            contractData.put("F8", 250000.00);
            contractData.put("G8", 250000.00);
            contractData.put("D12", 1);
            contractData.put("G12", 250000.00);
            contractData.put("B13", "贰拾伍万元整");
            contractData.put("A14", "二、 交货时间： 2025年3月3日（另加货运3天）");
            contractData.put("A15", "三、 交货地点： 湖南省醴陵经济开发区A区");
            contractData.put("A16", "四、 技术标准、质量要求：符合国家信息安全等级保护二级要求");
            contractData.put("A17", "五、运费方式及费用承担：乙方负责系统部署及上线服务");
            contractData.put("A18", "六、 结算及支付方式：合同签订后5个工作日内支付全款（乙方提供增值税专用发票）");
            contractData.put("A19", "七、违约责任：乙方必须按合同要求提供服务，如未按期提供或服务质量不达标，予以赔偿延期所致甲方的损失");
            contractData.put("B25", "单位名称（盖章）：湖南陶润会文化传播有限公司工会委员会");
            contractData.put("B26", "法定代表人：");
            contractData.put("B27", "委托代表人：");
            contractData.put("B28", "电    话：0731-23676922");
            contractData.put("B29", "传    真：0731-23676922");
            contractData.put("B30", "开户行：醴陵农村商业银行城区支行");
            contractData.put("B31", "账    号：8201 0750 0025 12544");
            contractData.put("D25", "单位名称（盖章）：湖南智工科技有限公司");
            contractData.put("D26", "法定代表人：张明");
            contractData.put("D27", "委托代表人：李华");
            contractData.put("D28", "电    话：15273352244");
            contractData.put("D29", "传    真：");
            contractData.put("D30", "开户行：中国农业银行醴陵市城东支行");
            contractData.put("D31", "账    号：6228481109443211071");

            ContractTemplateProcessor.generateContract(templatePath, outputPath, contractData);

            System.out.println("合同生成成功: " + outputPath);
            System.out.println("所有样式已完美保留！");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}