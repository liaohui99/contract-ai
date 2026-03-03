package com.example.springaiapp.tools;

import org.springframework.ai.chat.model.ToolContext;
import org.springframework.stereotype.Component;

import java.util.function.BiFunction;

/**
 * @author Gabriel
 * @version 1.0
 * @date 2026/2/9 17:10
 * @description: TODO
 */
@Component
public class WeatherTool implements BiFunction<String, ToolContext, String> {
    @Override
    public String apply(String city, ToolContext toolContext) {
        return "It's always sunny in " + city + "!";
    }
}