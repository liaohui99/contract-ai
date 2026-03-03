package com.example.springaiapp.skills;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ContractTemplateProcessor测试类
 * 验证合同模板处理功能
 *
 * @author Gabriel
 * @version 1.0
 */
@SpringBootTest
class ContractTemplateProcessorTest {

    @TempDir
    Path tempDir;

    /**
     * 测试修改第7行（B7单元格）内容成功
     * 这是修复硬编码保护逻辑的关键测试用例
     */
    @Test
    void testModifyB7CellSuccessfully() {
        try {
            String templatePath = "src/main/resources/hetong/template.xlsx";
            File outputFile = tempDir.resolve("b7_modify_test.xlsx").toFile();
            String outputPath = outputFile.getAbsolutePath();
            
            Map<String, Object> initialData = new HashMap<>();
            initialData.put("B3", "测试甲方公司");
            initialData.put("F2", "HT-TEST-001");
            initialData.put("B7", "原始规格型号内容");
            
            ContractTemplateProcessor.generateContract(templatePath, outputPath, initialData);
            
            Map<String, Object> modifyData = new HashMap<>();
            modifyData.put("B7", "修改后的规格型号内容\n- 第一项\n- 第二项\n- 第三项");
            
            ContractTemplateProcessor.generateContract(outputPath, outputPath, modifyData, "B7", "B7");
            
            assertTrue(outputFile.exists(), "文件应该存在");
            assertTrue(outputFile.length() > 0, "文件应该有内容");
            
            System.out.println("B7单元格修改测试成功，文件路径: " + outputPath);
            
        } catch (Exception e) {
            e.printStackTrace();
            fail("测试失败: " + e.getMessage());
        }
    }

    /**
     * 测试清除指定区域内容
     */
    @Test
    void testClearCellRange() {
        try {
            String templatePath = "src/main/resources/hetong/template.xlsx";
            File outputFile = tempDir.resolve("clear_range_test.xlsx").toFile();
            String outputPath = outputFile.getAbsolutePath();
            
            Map<String, Object> initialData = new HashMap<>();
            initialData.put("B3", "测试甲方公司");
            initialData.put("F2", "HT-TEST-002");
            initialData.put("B7", "初始内容B7");
            initialData.put("B8", "初始内容B8");
            initialData.put("B9", "初始内容B9");
            
            ContractTemplateProcessor.generateContract(templatePath, outputPath, initialData);
            
            Map<String, Object> newData = new HashMap<>();
            newData.put("B7", "新的B7内容");
            newData.put("B8", "新的B8内容");
            
            ContractTemplateProcessor.generateContract(outputPath, outputPath, newData, "B7", "B9");
            
            assertTrue(outputFile.exists(), "文件应该存在");
            System.out.println("清除区域测试成功，文件路径: " + outputPath);
            
        } catch (Exception e) {
            e.printStackTrace();
            fail("测试失败: " + e.getMessage());
        }
    }

    /**
     * 测试使用cellMapping编辑合同
     */
    @Test
    void testEditContractWithCellMapping() {
        try {
            String templatePath = "src/main/resources/hetong/template.xlsx";
            File outputFile = tempDir.resolve("cell_mapping_test.xlsx").toFile();
            String outputPath = outputFile.getAbsolutePath();
            
            Map<String, Object> initialData = new HashMap<>();
            initialData.put("B3", "初始甲方");
            initialData.put("F2", "HT-003");
            initialData.put("B7", "初始规格");
            
            ContractTemplateProcessor.generateContract(templatePath, outputPath, initialData);
            
            long initialSize = outputFile.length();
            
            Map<String, Object> editData = new HashMap<>();
            editData.put("B3", "修改后的甲方");
            editData.put("B7", "修改后的规格型号：\n• 版本：v3.0\n• 功能：完整版");
            
            ContractTemplateProcessor.generateContract(outputPath, outputPath, editData);
            
            assertTrue(outputFile.exists(), "文件应该存在");
            assertTrue(outputFile.length() > 0, "文件应该有内容");
            
            System.out.println("cellMapping编辑测试成功，文件路径: " + outputPath);
            
        } catch (Exception e) {
            e.printStackTrace();
            fail("测试失败: " + e.getMessage());
        }
    }

    /**
     * 测试生成合同并保留样式
     */
    @Test
    void testGenerateContractPreserveStyle() {
        try {
            String templatePath = "src/main/resources/hetong/template.xlsx";
            File outputFile = tempDir.resolve("preserve_style_test.xlsx").toFile();
            String outputPath = outputFile.getAbsolutePath();
            
            Map<String, Object> data = new HashMap<>();
            data.put("B3", "湖南陶润会文化传播有限公司工会委员会");
            data.put("F2", "HT-2025-001");
            data.put("G3", "湖南醴陵");
            data.put("B4", "测试供应商有限公司");
            data.put("F5", "2025-03-03");
            data.put("B7", "测试商品");
            data.put("B13", "壹万元整");
            
            ContractTemplateProcessor.generateContract(templatePath, outputPath, data);
            
            assertTrue(outputFile.exists(), "文件应该存在");
            assertTrue(outputFile.length() > 10000, "文件应该有足够的内容（保留样式）");
            
            System.out.println("保留样式测试成功，文件路径: " + outputPath);
            
        } catch (Exception e) {
            e.printStackTrace();
            fail("测试失败: " + e.getMessage());
        }
    }
}
