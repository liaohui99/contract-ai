package com.example.springaiapp;

import com.example.springaiapp.skills.ExcelGeneratorSkill;
import com.example.springaiapp.skills.ExcelWriteTool;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.HashMap;
import java.util.Map;

/**
 * 完整集成测试
 * 验证ExcelGeneratorSkill和ExcelWriteTool与ContractTemplateProcessor的集成
 */
@SpringBootTest
class CompleteIntegrationTest {

    @Test
    void testExcelGeneratorWithCellMapping() {
        try {
            ExcelGeneratorSkill generatorSkill = new ExcelGeneratorSkill(null);
            ExcelGeneratorSkill.GeneratorRequest request = new ExcelGeneratorSkill.GeneratorRequest();

            String templatePath = "src/main/resources/hetong/template.xlsx";
            String outputPath = "target/integration_excel_generator_with_style.xlsx";

            request.templatePath = templatePath;
            request.outputPath = outputPath;

            Map<String, Object> cellMapping = new HashMap<>();
            cellMapping.put("B3", "湖南陶润会文化传播有限公司工会委员会");
            cellMapping.put("F2", "INT-2025-001");
            cellMapping.put("G3", "湖南醴陵");
            cellMapping.put("B4", "测试集成供应商有限公司");
            cellMapping.put("F5", "2025-03-03");
            cellMapping.put("B8", "集成测试服务");
            cellMapping.put("C8", "完整功能测试");
            cellMapping.put("D8", 1);
            cellMapping.put("E8", "项");
            cellMapping.put("F8", 100000.00);
            cellMapping.put("G8", 100000.00);
            cellMapping.put("D12", 1);
            cellMapping.put("G12", 100000.00);
            cellMapping.put("B13", "壹拾万元整");
            cellMapping.put("A14", "二、 交货时间： 2025年3月3日（另加货运3天）");
            cellMapping.put("A15", "三、 交货地点： 集成测试地点");
            cellMapping.put("A16", "四、 技术标准、质量要求：符合集成测试标准");
            cellMapping.put("A17", "五、运费方式及费用承担：集成测试方负责");
            cellMapping.put("A18", "六、 结算及支付方式：集成测试后5个工作日内支付全款");
            cellMapping.put("A19", "七、违约责任：集成测试方必须按要求完成测试");

            request.cellMapping = cellMapping;

            String result = generatorSkill.apply(request, null);
            System.out.println("ExcelGeneratorSkill测试结果:");
            System.out.println(result);
            System.out.println("输出文件: " + outputPath);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    void testExcelWriteWithCellMapping() {
        try {
            ExcelWriteTool writeTool = new ExcelWriteTool();
            ExcelWriteTool.WriteRequest request = new ExcelWriteTool.WriteRequest();

            String templatePath = "src/main/resources/hetong/template.xlsx";
            String outputPath = "target/integration_excel_write_with_style.xlsx";

            request.templatePath = templatePath;
            request.filePath = outputPath;

            Map<String, Object> cellMapping = new HashMap<>();
            cellMapping.put("B3", "集成测试公司");
            cellMapping.put("F2", "WRITE-2025-001");
            cellMapping.put("G3", "集成测试地");
            cellMapping.put("B4", "ExcelWrite测试供应商");
            cellMapping.put("F5", "2025-03-03");
            cellMapping.put("B8", "ExcelWrite工具测试");
            cellMapping.put("C8", "功能验证");
            cellMapping.put("D8", 2);
            cellMapping.put("E8", "套");
            cellMapping.put("F8", 50000.00);
            cellMapping.put("G8", 100000.00);
            cellMapping.put("D12", 2);
            cellMapping.put("G12", 100000.00);
            cellMapping.put("B13", "壹拾万元整");

            request.cellMapping = cellMapping;

            String result = writeTool.apply(request, null);
            System.out.println("\nExcelWriteTool测试结果:");
            System.out.println(result);
            System.out.println("输出文件: " + outputPath);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}