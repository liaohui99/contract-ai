package com.example.springaiapp.controller;

import com.alibaba.cloud.ai.graph.RunnableConfig;
import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import com.alibaba.cloud.ai.graph.checkpoint.savers.MemorySaver;
import com.alibaba.cloud.ai.graph.exception.GraphRunnerException;
import com.alibaba.fastjson.JSON;
import com.example.springaiapp.service.AliBaiLianService;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.deepseek.api.ResponseFormat;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * AI聊天控制器
 * 用于测试AI模型的调用
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/test")
public class ChatTestController {

    private final AliBaiLianService aliBaiLianService;
    private final ReactAgent faruiReactAgent;
    private final ReactAgent contractReactAgent;
    private final ChatModel chatModel;
    private final List<ToolCallback> toolCallbacks;

    /**
     * 测试AI对话接口
     *
     * @param message 用户消息
     * @return AI响应
     */
    @GetMapping("/chat")
    public String chat(@RequestParam String message) {
        return aliBaiLianService.chat(message);
    }


    @GetMapping("/chat/react")
    public String chatReactAgent(@RequestParam String message) throws GraphRunnerException {
        AssistantMessage call = faruiReactAgent.call(message);
        return JSON.toJSONString(call);
    }

    @GetMapping("/chat/react/tools")
    public String chatToolsReactAgent(@RequestParam String message) throws GraphRunnerException {
        AssistantMessage call = contractReactAgent.call(message);
        return JSON.toJSONString(call);
    }

    @GetMapping("/chat/react/simple")
    public String chatSimpleToolsReactAgent(@RequestParam String message) throws GraphRunnerException {
        // 创建 agent
        ReactAgent agent = ReactAgent.builder()
                .name("weather_pun_agent")
                .model(chatModel)
                .systemPrompt("你是一个简单的天气助手，请根据用户的消息回答天气问题")
                .tools(toolCallbacks)
                //.outputType(ResponseFormat.class)
                .saver(new MemorySaver())
                .build();
        // threadId 是给定对话的唯一标识符
        RunnableConfig runnableConfig = RunnableConfig.builder().threadId(String.valueOf(Thread.currentThread().getId())).addMetadata("user_id", "1").build();

        AssistantMessage call = agent.call(message,runnableConfig);
        return JSON.toJSONString(call);
    }
}