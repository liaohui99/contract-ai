package com.example.springaiapp.controller;

import com.alibaba.cloud.ai.graph.NodeOutput;
import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.RunnableConfig;
import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import com.alibaba.cloud.ai.graph.exception.GraphRunnerException;
import com.alibaba.cloud.ai.graph.streaming.StreamingOutput;
import com.alibaba.fastjson.JSON;
import com.example.springaiapp.config.ApplicationConfig;
import com.example.springaiapp.req.MessageReq;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.MessageType;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;


@Slf4j
@RestController
@RequestMapping("/ai")
@RequiredArgsConstructor
public class ContractController {

    private final ReactAgent contractReactAgent;
    private final ReactAgent faruiReactAgent;
    private final ApplicationConfig applicationConfig;


    @PostMapping(value = "/farui/chat")
    public Flux<ServerSentEvent<String>> faruiChat(@ModelAttribute @Validated MessageReq req) throws GraphRunnerException {
        RunnableConfig config = RunnableConfig.builder()
                .threadId(req.getSessionId())
                .build();
        return faruiReactAgent.streamMessages(req.getQuestion(), config)
                .publishOn(Schedulers.boundedElastic())
                .concatMap(messageRsp -> {
                    // 检查消息类型
                    if (messageRsp.getMessageType() == MessageType.ASSISTANT) {
                        String text = messageRsp.getText();
                        if (text != null && !text.isEmpty()) {
                            System.out.print(text); // 实时打印助手消息内容
                            return Flux.just(ServerSentEvent.<String>builder()
                                    .event("data")
                                    .data(formatSseData("continue", text))
                                    .build());
                        }
                    }
                    // 返回空的事件以继续流
                    return Flux.empty();
                })
                .doOnComplete(() -> System.out.println("\n流式输出完成"))
                .doOnError(error -> System.err.println("\n流式输出错误: " + error.getMessage()));
    }


    @PostMapping(value = "/simple/chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<String>> simpleChat2(@ModelAttribute @Validated MessageReq req,
                                                     @RequestPart(value = "files", required = false) MultipartFile[] files)
            throws GraphRunnerException, IOException {
        RunnableConfig config = RunnableConfig.builder()
                .threadId(req.getSessionId())
                .build();

        HashMap<String, String> filesMap = new HashMap<>();
        String question = req.getQuestion();
        if (files != null && files.length > 0) {
            for (MultipartFile file : files) {
                try {
                    // 1. 生成唯一文件名
                    String originalName = file.getOriginalFilename();
                    String safeName = UUID.randomUUID() + "_" + originalName;

                    // 2. 确保目录存在
                    Path uploadPath = Paths.get(applicationConfig.getUploadDir());
                    if (!Files.exists(uploadPath)) {
                        Files.createDirectories(uploadPath);
                    }

                    // 3. 保存到本地
                    Path targetPath = uploadPath.resolve(safeName);
                    file.transferTo(targetPath.toFile());

                    // 4. 使用 Tika 提取文本
                    TikaDocumentReader reader = new TikaDocumentReader(new FileSystemResource(targetPath));
                    List<Document> documents = reader.get();
                    String text = documents.stream()
                            .map(Document::getText)
                            .collect(Collectors.joining("\n"));

                    log.info("提取的文件: {} \n 提取的文本: {}",safeName, text);
                    filesMap.put(targetPath.toFile().getPath(), text);
                    // 可选：如果需要保留文件，则不移除；若只需文本，可删除临时保存的文件
                    // Files.deleteIfExists(targetPath);

                } catch (IOException e) {
                    // 记录错误，但不中断其他文件处理
                    log.error("处理文件失败: {}", file.getOriginalFilename(), e);
                }
            }
            question += "\n\n  用户上传的文件路径及文件内容: \n  " + JSON.toJSON(filesMap);
        }

        return contractReactAgent.streamMessages(question, config)
                .publishOn(Schedulers.boundedElastic())
                .concatMap(messageRsp -> {
                    // 检查消息类型
                    if (messageRsp.getMessageType() == MessageType.ASSISTANT) {
                        String text = messageRsp.getText();
                        if (text != null && !text.isEmpty()) {
                            System.out.print(text); // 实时打印助手消息内容
                            return Flux.just(ServerSentEvent.<String>builder()
                                    .event("data")
                                    .data(formatSseData("continue", text))
                                    .build());
                        }
                    }

                    // 返回空的事件以继续流
                    return Flux.empty();
                })
                .doOnComplete(() -> System.out.println("流式输出完成"))
                .doOnError(error -> System.err.println("流式输出错误: " + error.getMessage()));
    }

    @GetMapping(value = "/simple/chat")
    public Flux<String> simpleChat(@RequestParam String message) throws GraphRunnerException {
       /* ApiKey apiKey = new SimpleApiKey(applicationConfig.getApiKey());
        ChatClient.CallResponseSpec responseSpec = chatClientBuilder.build().prompt()
                .advisors(new DashScopeDocumentAnalysisAdvisor(apiKey))
                .advisors(a -> a.param(DashScopeDocumentAnalysisAdvisor.RESOURCE, new ClassPathResource("hetong/template.xlsx")))
                .user("详细解析附件文件中的内容") //或根据文档内容提问
                .options(DashScopeChatOptions.builder().withModel("qwen-long").build())
                .call();
        System.out.println("messageText ： " + responseSpec.content());*/

        // 处理宠物的图片
       /*  if (files != null && files.length > 0) {

           ApiKey apiKey = new SimpleApiKey(applicationConfig.getApiKey());
            ChatClient.CallResponseSpec responseSpec = chatClientBuilder.build().prompt()
                    .advisors(new DashScopeDocumentAnalysisAdvisor(apiKey))
                    .advisors(a -> a.param(DashScopeDocumentAnalysisAdvisor.RESOURCE, new ClassPathResource("hetong/template.xlsx")))
                    .user("详细解析附件文件中的内容") //或根据文档内容提问
                    .options(DashScopeChatOptions.builder().withModel("qwen-long").build())
                    .call();
            System.out.println("messageText ： "+responseSpec.content());
        }*/


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


    @GetMapping(value = "/simple/chat2", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<String>> simpleChat3(@RequestParam String message) throws GraphRunnerException {
        RunnableConfig config = RunnableConfig.builder()
                .threadId("streaming_thread")
                .build();

        // 使用标志标记是否已发送最终结果，避免重复
        class ResultTracker {
            volatile boolean finalResultSent = false;
        }

        ResultTracker tracker = new ResultTracker();

        return contractReactAgent.stream(message, config)
                .publishOn(Schedulers.boundedElastic())
                .concatMap(output -> {
                    // 检查是否为流式输出
                    if (output instanceof StreamingOutput<?> streamingOutput) {
                        String chunk = streamingOutput.chunk();
                        if (chunk != null && !chunk.isEmpty()) {
                            System.out.print(chunk); // 实时打印流式内容
                            return Flux.just(ServerSentEvent.<String>builder()
                                    .event("data")
                                    .data(formatSseData("continue", chunk))
                                    .build());
                        }
                    } else {
                        // 检查是否有结果
                        String nodeId = output.node();
                        Map<String, Object> state = output.state().data();
                        System.out.println("\n 节点 '" + nodeId + "' 执行完成");

                        // 只处理特定节点的结果，并且只发送一次最终结果
                        if ("agentExecutor".equals(nodeId) && state.containsKey("result") && !tracker.finalResultSent) {
                            String result = state.get("result").toString();
                            System.out.println("最终结果: " + result);
                            tracker.finalResultSent = true;  // 设置标志，避免重复发送
                            return Flux.just(ServerSentEvent.<String>builder()
                                    .event("data")
                                    .data(formatSseData("end", result))
                                    .build());
                        }
                    }

                    // 返回空的事件以继续流
                    return Flux.empty();
                })
                .doOnComplete(() -> {
                    System.out.println("流式输出完成");
                    // 如果最终结果尚未发送，则发送完成事件
                    if (!tracker.finalResultSent) {
                        System.out.println("未接收到最终结果，发送完成事件");
                    }
                })
                .doOnError(error -> System.err.println("流式输出错误: " + error.getMessage()));
    }

    private String formatSseData(String messageType, String content) {
        return "{\"messageType\":\"" + messageType + "\",\"content\":\"" + escapeJson(content) + "\"}";
    }


}