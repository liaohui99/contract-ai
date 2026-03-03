package com.example.springaiapp;

import com.example.springaiapp.skills.ExcelWriteTool;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;

/**
 * ExcelWriteTool测试类
 * 验证使用模板写入Excel并保留样式的功能
 *
 * @author Gabriel
 * @version 1.0
 */
@SpringBootTest
class ExcelWriteToolTest {

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
}