package com.example.springaiapp.skills;

import org.springframework.ai.tool.ToolCallback;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 技能配置类
 * 配置和注册各种AI技能
 */
@Configuration
public class SelfSkillsConfig {

    private final ExcelGeneratorSkill excelGeneratorSkill;

    /**
     * 构造函数注入ExcelGeneratorSkill
     *
     * @param excelGeneratorSkill Excel生成工具
     */
    public SelfSkillsConfig(ExcelGeneratorSkill excelGeneratorSkill) {
        this.excelGeneratorSkill = excelGeneratorSkill;
    }

    /**
     * 注册Excel读取工具
     */
    @Bean
    public ToolCallback excelReadToolCallback() {
        return ExcelReadTool.createToolCallback();
    }

    /**
     * 注册Excel写入工具
     */
    @Bean
    public ToolCallback excelWriteToolCallback() {
        return ExcelWriteTool.createToolCallback();
    }

    /**
     * 注册Excel分析工具
     */
    @Bean
    public ToolCallback excelAnalysisToolCallback() {
        return ExcelAnalysisTool.createToolCallback();
    }

    /**
     * 注册Excel生成工具
     */
    @Bean
    public ToolCallback excelGeneratorSkillCallback() {
        return excelGeneratorSkill.createToolCallback();
    }

    /**
     * 注册合同生成工具
     */
    @Bean
    public ToolCallback contractGeneratorToolCallback() {
        return ContractGeneratorTool.createToolCallback();
    }
}