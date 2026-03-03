package com.example.springaiapp.config;

import com.alibaba.cloud.ai.graph.agent.interceptor.ToolCallHandler;
import com.alibaba.cloud.ai.graph.agent.interceptor.ToolCallRequest;
import com.alibaba.cloud.ai.graph.agent.interceptor.ToolCallResponse;
import com.alibaba.cloud.ai.graph.agent.interceptor.ToolInterceptor;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Gabriel
 * @version 1.0
 * @date 2026/2/10 11:22
 * @description: TODO
 */
@Slf4j
public class ToolErrorInterceptor extends ToolInterceptor {
    @Override
    public ToolCallResponse interceptToolCall(ToolCallRequest request, ToolCallHandler handler) {
        try {
            ToolCallResponse callResponse = handler.call(request);
            log.info("Tool call: {}", request.getToolName());
            log.info("Tool call result: {}", callResponse.getResult());
            return callResponse;
        } catch (Exception e) {
            return ToolCallResponse.of(request.getToolCallId(), request.getToolName(),
                    "Tool failed: " + e.getMessage());
        }
    }

    @Override
    public String getName() {
        return "ToolErrorInterceptor";
    }

}