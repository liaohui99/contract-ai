package com.example.springaiapp.controller;

import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/ai")
public class AIController {

    @Autowired
    private ChatModel chatModel;

    @GetMapping("/simple-chat")
    public String simpleChat(@RequestParam(defaultValue = "你好，请介绍一下自己") String message) {
        String response = chatModel.call(message);
        return response;
    }

    @GetMapping("/prompt-chat")
    public String promptChat(@RequestParam(defaultValue = "请解释一下人工智能") String message) {
        Prompt prompt = new Prompt(new org.springframework.ai.chat.messages.UserMessage(message));
        String response = chatModel.call(prompt).getResult().getOutput().getText();
        return response;
    }
}