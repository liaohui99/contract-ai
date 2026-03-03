package com.example.springaiapp.example;

import com.alibaba.cloud.ai.dashscope.api.DashScopeApi;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatModel;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import com.alibaba.cloud.ai.graph.exception.GraphRunnerException;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.function.FunctionToolCallback;
import reactor.core.publisher.Flux;

import java.util.List;

/**
 * @author Gabriel
 * @version 1.0
 * @date 2026/2/10 14:20
 * @description: TODO
 */
public class ModelExample {




    public void simpleChat(){

        // 创建 DashScope API 实例
        DashScopeApi dashScopeApi = DashScopeApi.builder()
                .apiKey(System.getenv("AI_DASHSCOPE_API_KEY"))
                .build();

        // 创建 ChatModel
        ChatModel chatModel = DashScopeChatModel.builder()
                .dashScopeApi(dashScopeApi)
                .build();

        // 使用字符串直接调用
        String response = chatModel.call("介绍一下Spring框架");
        System.out.println(response);


    }


    public void promptChat(){
        // 创建 Prompt
        Prompt prompt = new Prompt(new UserMessage("解释什么是微服务架构"));

        // 创建 DashScope API 实例
        DashScopeApi dashScopeApi = DashScopeApi.builder()
                .apiKey(System.getenv("AI_DASHSCOPE_API_KEY"))
                .build();

        // 创建 ChatModel
        ChatModel chatModel = DashScopeChatModel.builder()
                .dashScopeApi(dashScopeApi)
                .build();
        //chatModel.stream("");

        // 调用并获取响应
        ChatResponse response = chatModel.call(prompt);
        String answer = response.getResult().getOutput().getText();
        System.out.println(answer);

    }


    public void chatOptionsChat(){
        DashScopeChatOptions options = DashScopeChatOptions.builder()
                .withModel("qwen-plus")           // 模型名称
                .withTemperature(0.7)              // Temperature 参数
                .withMaxToken(2000)                // 最大令牌数
                .withTopP(0.9)                     // Top-P 采样
                .build();

        // 创建 Prompt
        Prompt prompt = new Prompt(new UserMessage("解释什么是微服务架构"));

        // 创建 DashScope API 实例
        DashScopeApi dashScopeApi = DashScopeApi.builder()
                .apiKey(System.getenv("AI_DASHSCOPE_API_KEY"))
                .build();

        // 创建 ChatModel
        ChatModel chatModel = DashScopeChatModel.builder()
                .dashScopeApi(dashScopeApi)
                .defaultOptions(options)
                .build();
        //chatModel.stream("");

        // 调用并获取响应
        ChatResponse response = chatModel.call(prompt);
        String answer = response.getResult().getOutput().getText();
        System.out.println(answer);

    }



    public void chatOptionsRunTimeChat(){
        // 创建带有特定选项的 Prompt
        DashScopeChatOptions runtimeOptions = DashScopeChatOptions.builder()
                .withTemperature(0.3)  // 更低的温度，更确定的输出
                .withMaxToken(500)
                .build();


        // 创建 Prompt
        Prompt prompt = new Prompt(
                new UserMessage("用一句话总结Java的特点"),
                runtimeOptions
        );
        // 创建 DashScope API 实例
        DashScopeApi dashScopeApi = DashScopeApi.builder()
                .apiKey(System.getenv("AI_DASHSCOPE_API_KEY"))
                .build();

        // 创建 ChatModel
        ChatModel chatModel = DashScopeChatModel.builder()
                .dashScopeApi(dashScopeApi)
                .build();
        //chatModel.stream("");

        // 调用并获取响应
        ChatResponse response = chatModel.call(prompt);
        String answer = response.getResult().getOutput().getText();
        System.out.println(answer);

    }

    public void streamChat(){
        // 创建 DashScope API 实例
        DashScopeApi dashScopeApi = DashScopeApi.builder()
                .apiKey(System.getenv("AI_DASHSCOPE_API_KEY"))
                .build();

        // 创建 ChatModel
        ChatModel chatModel = DashScopeChatModel.builder()
                .dashScopeApi(dashScopeApi)
                .build();
        //chatModel.stream("");

        // 调用并获取响应
        // 使用流式 API
        Flux<ChatResponse> responseStream = chatModel.stream(
                new Prompt("详细解释Spring Boot的自动配置原理")
        );

        // 订阅并处理流式响应
        responseStream.subscribe(
                chatResponse -> {
                    String content = chatResponse.getResult()
                            .getOutput()
                            .getText();
                    System.out.print(content);
                },
                error -> System.err.println("错误: " + error.getMessage()),
                () -> System.out.println("流式响应完成")
                );

    }


    //多轮对话
    public void multiRoundChat(){
        // 创建 DashScope API 实例
        DashScopeApi dashScopeApi = DashScopeApi.builder()
                .apiKey(System.getenv("AI_DASHSCOPE_API_KEY"))
                .build();

        // 创建 ChatModel
        ChatModel chatModel = DashScopeChatModel.builder()
                .dashScopeApi(dashScopeApi)
                .build();

        // 创建对话历史
        List<Message> messages = List.of(
                new SystemMessage("你是一个Java专家"),
                new UserMessage("什么是Spring Boot?"),
                new AssistantMessage("Spring Boot是..."),
                new UserMessage("它有什么优势?")
        );
        Prompt prompt = new Prompt(messages);
        chatModel.stream(prompt);
        ChatResponse response = chatModel.call(prompt);

        String answer = response.getResult().getOutput().getText();
        System.out.println(answer);

    }

    //Function Calling
    public void functionCalling(){
        ToolCallback weatherFunction = FunctionToolCallback.builder("getWeather", (String city) -> {
                    // 实际的天气查询逻辑
                    return "晴朗，25°C";
                })
                .description("获取指定城市的天气")
                .inputType(String.class)
                .build();

        // 创建 DashScope API 实例
        DashScopeApi dashScopeApi = DashScopeApi.builder()
                .apiKey(System.getenv("AI_DASHSCOPE_API_KEY"))
                .build();

        // 使用函数
        DashScopeChatOptions options = DashScopeChatOptions.builder()
                .withToolCallbacks(List.of(weatherFunction))
                //.withTools(List.of(weatherFunction))
                .build();

        // 创建 ChatModel
        ChatModel chatModel = DashScopeChatModel.builder()
                .dashScopeApi(dashScopeApi)
                //.defaultOptions(options)
                .build();



        Prompt prompt = new Prompt("北京的天气怎么样?", options);
        ChatResponse response = chatModel.call(prompt);

    }


    //与 ReactAgent 集成
    public void integrateWithReactAgent() throws GraphRunnerException {
        // 创建 DashScope API 实例
        DashScopeApi dashScopeApi = DashScopeApi.builder()
                .apiKey(System.getenv("AI_DASHSCOPE_API_KEY"))
                .build();

        // 创建 ChatModel
        ChatModel chatModel = DashScopeChatModel.builder()
                .dashScopeApi(dashScopeApi)
                .build();

        ReactAgent agent = ReactAgent.builder()
                .name("my_agent")
                .model(chatModel)
                .systemPrompt("你是一个有帮助的AI助手")
                .build();

        // 调用 Agent
        AssistantMessage response = agent.call("帮我分析这个问题");

    }


}