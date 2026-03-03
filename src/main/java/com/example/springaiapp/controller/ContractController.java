package com.example.springaiapp.controller;

import com.alibaba.cloud.ai.graph.NodeOutput;
import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.RunnableConfig;
import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import com.alibaba.cloud.ai.graph.exception.GraphRunnerException;
import com.alibaba.cloud.ai.graph.streaming.StreamingOutput;
import com.alibaba.fastjson.JSON;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.io.IOException;
import java.util.Map;


@RestController
@RequestMapping("/ai")
public class ContractController {

    @Autowired
    private ReactAgent contractReactAgent;

    @GetMapping(value = "/simple/chat")
    public Flux<String> simpleChat(@RequestParam String message) throws GraphRunnerException {
        return contractReactAgent.stream(message)
                .map(nodeOutput -> {
                    // 获取当前状态
                    OverAllState state = nodeOutput.state();

                    // 判断是否为最终输出（可根据实际节点名判断）
                    if (isFinalNode(nodeOutput)) {
                        // 从状态中提取最终答案
                        String finalAnswer = extractFinalAnswer(state);
                        return formatSseMessage("end", finalAnswer);
                    }

                    // 中间输出（如推理过程、工具调用日志）
                    String partial = extractPartialOutput(nodeOutput, state);
                    return formatSseMessage("continue", partial);
                });
    }



    // 辅助方法：判断是否为最终回答节点（根据您的图定义调整）
    private boolean isFinalNode(NodeOutput nodeOutput) {
        // 常见的最终节点名可能是 "agentExecutor"、"finalNode" 等
        return "agentExecutor".equals(nodeOutput.node());
    }

    // 辅助方法：从状态中提取最终答案（根据您的实际状态结构调整）
    private String extractFinalAnswer(OverAllState state) {
        // 示例：假设最终答案存储在 state 的 "output" 字段中
        Object output = state.value("output");
        return output != null ? output.toString() : "";
    }

    // 辅助方法：提取中间过程的输出（例如 LLM 的流式内容）
    private String extractPartialOutput(NodeOutput nodeOutput, OverAllState state) {
        // 如果节点本身有内容（如 LLM 生成的文本块），可直接使用
        // 否则可以从 state 的 "messages" 等字段获取最新消息
        // 默认返回节点名称 + 状态摘要
        return "[" + nodeOutput.node() + "] " + state.toString();
    }

    // 格式化 SSE 消息
    private String formatSseMessage(String messageType, String content) {
        // 构造 JSON 格式，方便前端解析
        return "data: " + String.format("{\"messageType\":\"%s\",\"content\":\"%s\"}", messageType, escapeJson(content)) + "\n\n";
    }

    private String escapeJson(String s) {
        return s.replace("\"", "\\\"").replace("\n", "\\n");
    }


    @GetMapping(value = "/simple/chat2")
    public void simpleChat2(@RequestParam String message, HttpServletResponse response) throws GraphRunnerException {
        // 创建配置
        RunnableConfig config = RunnableConfig.builder()
                .threadId("streaming_thread")
                .build();

        response.setContentType("text/event-stream");
        response.setCharacterEncoding("UTF-8");


        contractReactAgent.stream(message, config)
                .publishOn(Schedulers.boundedElastic())
                .doOnNext(output -> {
                    // 处理流式输出
                    if (output instanceof StreamingOutput<?> streamingOutput) {
                        // 流式输出块
                        String chunk = streamingOutput.chunk();
                        if (chunk != null && !chunk.isEmpty()) {
                            System.out.print(chunk); // 实时打印流式内容
                            try {
                                response.getWriter().write(formatSseMessage("continue", chunk)); // Write to response stream
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    } else {
                        // 普通节点输出
                        String nodeId = output.node();
                        Map<String, Object> state = output.state().data();
                        System.out.println("\n 节点 '" + nodeId + "' 执行完成");
                        if (state.containsKey("result")) {
                            System.out.println("最终结果: " + state.get("result"));
                        }
                    }
                })
                .doOnComplete(() -> {
                    System.out.println("流式输出完成");
                })
                .doOnError(error -> {
                    System.err.println("流式输出错误: " + error.getMessage());
                })
                .blockLast(); // 阻塞等待流完成

    }


}