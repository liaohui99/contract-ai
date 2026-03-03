package com.example.springaiapp.tools;

import org.springframework.ai.chat.model.ToolContext;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.function.BiFunction;

/**
 * @author Gabriel
 * @version 1.0
 * @date 2026/2/9 17:10
 * @description: TODO
 */
@Component
public class TimeTool implements BiFunction<String, ToolContext, String> {
    @Override
    public String apply(String city, ToolContext toolContext) {
        // 格式化输出当前时间
        return LocalDateTime.now().toString();
    }
}