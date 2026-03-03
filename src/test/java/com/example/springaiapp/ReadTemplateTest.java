package com.example.springaiapp;

import com.example.springaiapp.skills.ExcelReadTool;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * 读取合同模板并分析结构
 */
@SpringBootTest
class ReadTemplateTest {

    @Test
    void testReadContractTemplate() {
        try {
            ExcelReadTool readTool = new ExcelReadTool();
            ExcelReadTool.ReadRequest request = new ExcelReadTool.ReadRequest();
            request.filePath = "src/main/resources/hetong/template.xlsx";
            request.sheetName = null;
            request.startRow = 0;
            request.endRow = 40;
            request.startCol = 0;
            request.endCol = 20;
            request.hasHeader = false;

            String result = readTool.apply(request, null);
            System.out.println("模板读取结果:");
            System.out.println(result);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}