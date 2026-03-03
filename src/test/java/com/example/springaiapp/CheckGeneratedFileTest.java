package com.example.springaiapp;

import com.example.springaiapp.skills.ExcelReadTool;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class CheckGeneratedFileTest {

    @Test
    void testCheckGeneratedFile() {
        try {
            ExcelReadTool readTool = new ExcelReadTool();
            ExcelReadTool.ReadRequest request = new ExcelReadTool.ReadRequest();
            request.filePath = "target/contract_with_clear_template_data.xlsx";
            request.sheetName = null;
            request.startRow = 0;
            request.endRow = 20;
            request.startCol = 0;
            request.endCol = 10;
            request.hasHeader = false;

            String result = readTool.apply(request, null);
            System.out.println("生成的文件读取结果:");
            System.out.println(result);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
