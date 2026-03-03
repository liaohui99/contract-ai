package com.example.springaiapp;

import com.example.springaiapp.skills.ExcelWriteTool;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ExcelWriteTool测试类
 * 验证使用模板写入Excel并保留样式的功能
 *
 * @author Gabriel
 * @version 1.0
 */
@SpringBootTest
class ExcelWriteToolTest {

    @TempDir
    Path tempDir;

    /**
     * 测试使用模板写入Excel并保留样式
     */
    @Test
    void testWriteExcelWithTemplate() {
        try {
            ExcelWriteTool writeTool = new ExcelWriteTool();
            ExcelWriteTool.WriteRequest request = new ExcelWriteTool.WriteRequest();

            String templatePath = "src/main/resources/hetong/template.xlsx";
            String outputPath = "target/test_output_with_style.xlsx";

            request.templatePath = templatePath;
            request.filePath = outputPath;
            request.sheetName = "Sheet1";
            request.appendMode = false;

            List<List<Object>> data = new ArrayList<>();
            List<Object> row1 = new ArrayList<>();
            row1.add("测试数据1");
            row1.add("测试数据2");
            row1.add("测试数据3");
            data.add(row1);

            List<Object> row2 = new ArrayList<>();
            row2.add("数据A");
            row2.add("数据B");
            row2.add("数据C");
            data.add(row2);

            request.data = data;

            String result = writeTool.apply(request, null);
            System.out.println("测试结果: " + result);
            System.out.println("输出文件: " + outputPath);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 测试编辑已存在文件时自动保护原有内容
     * 这是修复覆盖问题的关键测试用例
     */
    @Test
    void testAutoProtectExistingFile() {
        try {
            ExcelWriteTool writeTool = new ExcelWriteTool();
            
            String templatePath = "src/main/resources/hetong/template.xlsx";
            File outputFile = tempDir.resolve("auto_protect_test.xlsx").toFile();
            String outputPath = outputFile.getAbsolutePath();
            
            ExcelWriteTool.WriteRequest createRequest = new ExcelWriteTool.WriteRequest();
            createRequest.templatePath = templatePath;
            createRequest.filePath = outputPath;
            
            Map<String, Object> initialData = new HashMap<>();
            initialData.put("B3", "原始甲方名称");
            initialData.put("F2", "HT-001");
            initialData.put("B13", "壹万元整");
            createRequest.cellMapping = initialData;
            
            String createResult = writeTool.apply(createRequest, null);
            System.out.println("创建文件结果: " + createResult);
            assertTrue(createResult.contains("\"success\": true"), "创建文件应该成功");
            
            long initialSize = outputFile.length();
            System.out.println("初始文件大小: " + initialSize);
            assertTrue(initialSize > 0, "文件应该有内容");
            
            ExcelWriteTool.WriteRequest editRequest = new ExcelWriteTool.WriteRequest();
            editRequest.filePath = outputPath;
            
            List<List<Object>> newData = new ArrayList<>();
            List<Object> newRow = new ArrayList<>();
            newRow.add("新添加的一行数据");
            newData.add(newRow);
            editRequest.data = newData;
            
            String editResult = writeTool.apply(editRequest, null);
            System.out.println("编辑文件结果: " + editResult);
            assertTrue(editResult.contains("\"success\": true"), "编辑文件应该成功");
            assertTrue(editResult.contains("\"autoProtected\": true"), "应该触发自动保护");
            
            long editedSize = outputFile.length();
            System.out.println("编辑后文件大小: " + editedSize);
            
            assertTrue(editedSize > 1000, "编辑后文件应该保留原有内容（大小应大于1KB）");
            
        } catch (Exception e) {
            e.printStackTrace();
            fail("测试失败: " + e.getMessage());
        }
    }

    /**
     * 测试新建文件时不会触发自动保护
     */
    @Test
    void testNewFileNotAutoProtected() {
        try {
            ExcelWriteTool writeTool = new ExcelWriteTool();
            
            File outputFile = tempDir.resolve("new_file_test.xlsx").toFile();
            String outputPath = outputFile.getAbsolutePath();
            
            ExcelWriteTool.WriteRequest request = new ExcelWriteTool.WriteRequest();
            request.filePath = outputPath;
            request.sheetName = "TestSheet";
            
            List<String> headers = new ArrayList<>();
            headers.add("姓名");
            headers.add("年龄");
            request.headers = headers;
            
            List<List<Object>> data = new ArrayList<>();
            List<Object> row1 = new ArrayList<>();
            row1.add("张三");
            row1.add(25);
            data.add(row1);
            request.data = data;
            
            String result = writeTool.apply(request, null);
            System.out.println("新建文件结果: " + result);
            assertTrue(result.contains("\"success\": true"), "新建文件应该成功");
            assertFalse(result.contains("\"autoProtected\": true"), "新建文件不应触发自动保护");
            
        } catch (Exception e) {
            e.printStackTrace();
            fail("测试失败: " + e.getMessage());
        }
    }

    /**
     * 测试使用cellMapping编辑已存在文件
     */
    @Test
    void testEditExistingFileWithCellMapping() {
        try {
            ExcelWriteTool writeTool = new ExcelWriteTool();
            
            String templatePath = "src/main/resources/hetong/template.xlsx";
            File outputFile = tempDir.resolve("cell_mapping_edit_test.xlsx").toFile();
            String outputPath = outputFile.getAbsolutePath();
            
            ExcelWriteTool.WriteRequest createRequest = new ExcelWriteTool.WriteRequest();
            createRequest.templatePath = templatePath;
            createRequest.filePath = outputPath;
            
            Map<String, Object> initialData = new HashMap<>();
            initialData.put("B3", "初始甲方");
            initialData.put("F2", "HT-002");
            createRequest.cellMapping = initialData;
            
            writeTool.apply(createRequest, null);
            
            ExcelWriteTool.WriteRequest editRequest = new ExcelWriteTool.WriteRequest();
            editRequest.templatePath = outputPath;
            editRequest.filePath = outputPath;
            
            Map<String, Object> editData = new HashMap<>();
            editData.put("B3", "修改后的甲方");
            editData.put("A16", "四、技术标准、质量要求：系统可用性不低于99.5%");
            editRequest.cellMapping = editData;
            
            String editResult = writeTool.apply(editRequest, null);
            System.out.println("使用cellMapping编辑结果: " + editResult);
            assertTrue(editResult.contains("\"success\": true"), "编辑应该成功");
            assertTrue(editResult.contains("\"preserveStyle\": true"), "应该保留样式");
            
        } catch (Exception e) {
            e.printStackTrace();
            fail("测试失败: " + e.getMessage());
        }
    }
}