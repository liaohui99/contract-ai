package com.example.springaiapp.skills;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.function.FunctionToolCallback;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

/**
 * 批量Excel写入工具
 * 用于处理包含大量文本内容的Excel写入操作，避免JSON解析错误
 *
 * @author Gabriel
 * @version 1.0
 */
@Slf4j
@Component
public class BatchExcelWriteTool implements BiFunction<BatchExcelWriteTool.BatchWriteRequest, ToolContext, String> {

    /**
     * 分批写入Excel文件
     *
     * @param request 写入请求参数
     * @param context 工具上下文
     * @return 写入结果JSON字符串
     */
    @Override
    public String apply(BatchWriteRequest request, ToolContext context) {
        log.info("开始分批写入Excel文件: {}", request.filePath);

        try {
            // 验证必需参数
            if (request == null) {
                throw new IllegalArgumentException("请求参数不能为空");
            }
            if (request.filePath == null || request.filePath.trim().isEmpty()) {
                throw new IllegalArgumentException("文件路径不能为空");
            }

            String filePath = request.filePath;
            String sheetName = request.sheetName != null ? request.sheetName : "Sheet1";
            Map<String, Object> cellMapping = request.cellMapping;
            int batchSize = request.batchSize != null ? request.batchSize : 10; // 默认每批处理10个单元格

            // 将单元格映射分批处理
            if (cellMapping != null && !cellMapping.isEmpty()) {
                ExcelWriteTool.WriteRequest writeRequest = new ExcelWriteTool.WriteRequest();
                writeRequest.filePath = filePath;
                writeRequest.sheetName = sheetName;
                
                // 分批处理单元格映射
                int processedCount = 0;
                for (Map.Entry<String, Object> entry : cellMapping.entrySet()) {
                    if (writeRequest.cellMapping == null) {
                        writeRequest.cellMapping = new HashMap<>();
                    }
                    
                    writeRequest.cellMapping.put(entry.getKey(), entry.getValue());
                    processedCount++;
                    
                    // 达到批次大小或处理完所有数据时执行写入
                    if (processedCount % batchSize == 0 || processedCount == cellMapping.size()) {
                        // 执行单次写入操作
                        ExcelWriteTool writeTool = new ExcelWriteTool();
                        writeTool.apply(writeRequest, context);
                        
                        // 重置cellMapping准备下一组
                        writeRequest.cellMapping = new HashMap<>();
                    }
                }
            }

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("filePath", filePath);
            response.put("sheetName", sheetName);
            response.put("totalCells", cellMapping != null ? cellMapping.size() : 0);
            response.put("batchSize", batchSize);
            response.put("message", "分批Excel写入成功完成");

            log.info("分批Excel文件写入成功，总处理单元格数: {}", cellMapping != null ? cellMapping.size() : 0);
            return toJson(response);

        } catch (Exception e) {
            log.error("分批写入Excel文件失败: {}", e.getMessage(), e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            errorResponse.put("message", "分批写入Excel文件失败: " + e.getMessage());
            return toJson(errorResponse);
        }
    }

    /**
     * 简单的JSON序列化
     */
    private String toJson(Map<String, Object> map) {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        boolean first = true;
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            if (!first) {
                sb.append(", ");
            }
            first = false;
            sb.append("\"").append(entry.getKey()).append("\": ");
            Object value = entry.getValue();
            if (value == null) {
                sb.append("null");
            } else if (value instanceof String) {
                sb.append("\"").append(escapeJson((String) value)).append("\"");
            } else {
                sb.append(value);
            }
        }
        sb.append("}");
        return sb.toString();
    }

    /**
     * JSON字符串转义
     */
    private String escapeJson(String str) {
        return str.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    /**
     * 工厂方法：创建工具回调
     */
    public static ToolCallback createToolCallback() {
        return FunctionToolCallback.builder("batch_excel_write", new BatchExcelWriteTool())
                .description("分批写入数据到Excel文件，专为处理大量文本内容设计以避免JSON解析错误。" +
                        "参数包括: filePath文件路径(必填), sheetName工作表名称(可选,默认Sheet1), " +
                        "cellMapping单元格映射(可选,Map格式,key为单元格位置如\"A1\",value为值), " +
                        "batchSize每批处理单元格数量(可选,默认10个)。" +
                        "【安全】将大数据集分解为小批次处理，避免JSON解析错误。")
                .inputType(BatchWriteRequest.class)
                .build();
    }

    /**
     * 分批写入请求参数
     */
    public static class BatchWriteRequest {
        @JsonProperty(required = true)
        @JsonPropertyDescription("Excel文件的完整路径")
        public String filePath;

        @JsonProperty
        @JsonPropertyDescription("工作表名称，默认Sheet1")
        public String sheetName;

        @JsonProperty
        @JsonPropertyDescription("单元格映射，Map格式，key为单元格位置如\"A1\"，value为值。此工具会将大数据集分批处理以避免JSON解析错误")
        public Map<String, Object> cellMapping;

        @JsonProperty
        @JsonPropertyDescription("每批处理的单元格数量，默认10个，可根据数据复杂度调整")
        public Integer batchSize;
    }
}