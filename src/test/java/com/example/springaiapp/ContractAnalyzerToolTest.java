package com.example.springaiapp;

import com.example.springaiapp.skills.ContractAnalyzerTool;
import com.example.springaiapp.skills.ContractAnalyzerTool.AnalyzeRequest;
import com.example.springaiapp.skills.ContractAnalyzerTool.ContractInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.io.File;

/**
 * ContractAnalyzerTool测试类
 *
 * @author Gabriel
 * @version 1.0
 */
public class ContractAnalyzerToolTest {

    private final ContractAnalyzerTool tool = new ContractAnalyzerTool();
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 测试解析Excel格式合同
     */
    @Test
    public void testAnalyzeExcelContract() {
        String filePath = "output/测试合同-HT-2025-001.xlsx";
        File file = new File(filePath);
        
        if (!file.exists()) {
            System.out.println("测试文件不存在，跳过测试: " + filePath);
            return;
        }

        AnalyzeRequest request = new AnalyzeRequest();
        request.setFilePath(filePath);

        String result = tool.apply(request, null);
        System.out.println("解析结果:");
        System.out.println(result);

        try {
            @SuppressWarnings("unchecked")
            java.util.Map<String, Object> resultMap = objectMapper.readValue(result, java.util.Map.class);
            Boolean success = (Boolean) resultMap.get("success");
            if (success != null && success) {
                System.out.println("\n✅ Excel合同解析测试通过");
            } else {
                System.out.println("\n❌ Excel合同解析测试失败: " + resultMap.get("error"));
            }
        } catch (Exception e) {
            System.out.println("\n❌ 解析结果失败: " + e.getMessage());
        }
    }

    /**
     * 测试解析不存在的文件
     */
    @Test
    public void testAnalyzeNonExistentFile() {
        AnalyzeRequest request = new AnalyzeRequest();
        request.setFilePath("non_existent_file.xlsx");

        String result = tool.apply(request, null);
        System.out.println("解析不存在的文件结果:");
        System.out.println(result);

        if (result.contains("\"success\": false")) {
            System.out.println("\n✅ 不存在的文件测试通过（正确返回错误）");
        } else {
            System.out.println("\n❌ 不存在的文件测试失败");
        }
    }

    /**
     * 测试解析不支持的文件格式
     */
    @Test
    public void testAnalyzeUnsupportedFormat() {
        AnalyzeRequest request = new AnalyzeRequest();
        request.setFilePath("test.pdf");

        String result = tool.apply(request, null);
        System.out.println("解析不支持的格式结果:");
        System.out.println(result);

        if (result.contains("\"success\": false") && result.contains("不支持的文件格式")) {
            System.out.println("\n✅ 不支持的格式测试通过（正确返回错误）");
        } else {
            System.out.println("\n❌ 不支持的格式测试失败");
        }
    }

    public static void main(String[] args) {
        ContractAnalyzerToolTest test = new ContractAnalyzerToolTest();
        System.out.println("========== 测试Excel合同解析 ==========");
        test.testAnalyzeExcelContract();
        System.out.println("\n========== 测试不存在的文件 ==========");
        test.testAnalyzeNonExistentFile();
        System.out.println("\n========== 测试不支持的格式 ==========");
        test.testAnalyzeUnsupportedFormat();
    }
}
