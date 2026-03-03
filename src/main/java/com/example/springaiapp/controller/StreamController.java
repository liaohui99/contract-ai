package com.example.springaiapp.controller;

import com.alibaba.cloud.ai.graph.NodeOutput;
import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import com.alibaba.cloud.ai.graph.exception.GraphRunnerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;

@RestController
@RequestMapping("/stream")
public class StreamController {

    @Autowired
    private ReactAgent contractReactAgent;

    /**
     * ReactAgent流式响应实现
     */
    @GetMapping(value = "/react-agent-stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<String>> streamReactAgent(@RequestParam String message) throws GraphRunnerException {
        return contractReactAgent.stream(Map.of("input", message))
                .map(nodeOutput -> {
                    // 获取输出内容
                    Object outputData = nodeOutput.agent();
                    String content = outputData != null ? outputData.toString() : "";
                    
                    return ServerSentEvent.<String>builder(content)
                            .event("data")
                            .build();
                })
                .concatWith(Mono.fromSupplier(() -> 
                    ServerSentEvent.<String>builder("")
                            .event("complete")
                            .build()))
                .onErrorResume(e -> Flux.just(
                        ServerSentEvent.<String>builder("Error: " + e.getMessage())
                                .event("error")
                                .build()
                ));
    }
}