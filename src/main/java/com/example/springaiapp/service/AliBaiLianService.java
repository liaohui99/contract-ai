package com.example.springaiapp.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.alibaba.cloud.ai.dashscope.api.DashScopeApi;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatModel;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;

/**
 * 阿里云百炼服务类
 * 用于封装阿里云百炼API的调用
 */
@Service
public class AliBaiLianService {

    private static final Logger logger = LoggerFactory.getLogger(AliBaiLianService.class);
    private final ChatModel chatModel;

    /**
     * 构造函数，初始化ChatModel
     */
    public AliBaiLianService(@Value("${spring.ai.dashscope.api-key}") String apiKey) {
        logger.info("初始化DashScope API，API Key: {}", apiKey != null ? "已设置" : "未设置");
        
        DashScopeApi dashScopeApi = DashScopeApi.builder()
                .apiKey(apiKey)
                .build();

        this.chatModel = DashScopeChatModel.builder()
                .dashScopeApi(dashScopeApi)
                .defaultOptions(DashScopeChatOptions.builder()
                        .withModel(DashScopeChatModel.DEFAULT_MODEL_NAME)
                        .withTemperature(0.7)
                        .build())
                .build();
    }

    /**
     * 测试API调用
     * @param message 用户消息
     * @return AI响应
     */
    public String chat(String message) {
        try {
            logger.info("收到聊天请求: {}", message);
            Prompt prompt = new Prompt(new UserMessage(message));
            String response = chatModel.call(prompt).getResult().getOutput().getText();
            logger.info("AI响应: {}", response);
            return response;
        } catch (Exception e) {
            logger.error("调用AI API时发生错误", e);
            return "错误: " + e.getMessage();
        }
    }
}