package com.example.springaiapp.config;

import com.example.springaiapp.tools.TimeTool;
import com.example.springaiapp.tools.UserLocationTool;
import com.example.springaiapp.tools.WeatherTool;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.function.FunctionToolCallback;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

/**
 * 工具配置类，包括本地工具和MCP工具
 */
@Configuration
public class ToolsConfig {

    @Bean
    public ToolCallback weatherToolCallBack(WeatherTool weatherTool){
        return FunctionToolCallback.builder("get_weather", weatherTool)
                .description("Get weather for a given city")
                .inputType(String.class)
                .build();
    }

    @Bean
    public ToolCallback timeToolCallBack(TimeTool timeTool){
        return FunctionToolCallback.builder("get_time", timeTool)
                .description("Obtain the current time")
                .inputType(String.class)
                .build();
    }

    @Bean
    public ToolCallback userLocationToolCallBack(UserLocationTool userLocationTool){
        return FunctionToolCallback.builder("get_user_local", userLocationTool)
                .description("Get the user's current location")
                .inputType(String.class)
                .build();
    }
    
/*    *//**
     * 将MCP工具回调作为列表提供，以便在agent中使用
     *//*
    @Bean
    public List<ToolCallback> allToolCallbacks(
            ToolCallback weatherToolCallBack,
            ToolCallback timeToolCallBack,
            ToolCallback userLocationToolCallBack,
            SyncMcpToolCallbackProvider mcpToolCallbackProvider) {
        
        List<ToolCallback> allTools = new ArrayList<>();
        
        // 添加本地工具
        allTools.add(weatherToolCallBack);
        allTools.add(timeToolCallBack);
        allTools.add(userLocationToolCallBack);
        
        // 添加MCP工具
        ToolCallback[] mcpTools = mcpToolCallbackProvider.getToolCallbacks();
        if (mcpTools != null) {
            for (ToolCallback tool : mcpTools) {
                allTools.add(tool);
            }
        }
        
        return allTools;
    }*/
}