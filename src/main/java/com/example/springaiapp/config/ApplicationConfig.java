package com.example.springaiapp.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * 应用配置类
 * 使用@Value注解注入application.yml中的所有配置项
 * 
 * @author Calendar Chart Team
 */
@Data
@Configuration
public class ApplicationConfig {

    // Spring应用配置
    @Value("${spring.application.name:springaiapp}")
    private String applicationName;

    // Redis配置
    @Value("${spring.redis.host:localhost}")
    private String redisHost;

    @Value("${spring.redis.port:6379}")
    private int redisPort;

    @Value("${spring.redis.database:0}")
    private int redisDatabase;

    @Value("${spring.redis.password:}")
    private String redisPassword;

    // 数据源配置
    @Value("${spring.datasource.url:jdbc:h2:mem:calendar_db;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE}")
    private String datasourceUrl;

    @Value("${spring.datasource.driver-class-name:org.h2.Driver}")
    private String datasourceDriverClassName;

    @Value("${spring.datasource.username:sa}")
    private String datasourceUsername;

    @Value("${spring.datasource.password:password}")
    private String datasourcePassword;

    // H2控制台配置
    @Value("${spring.h2.console.enabled:true}")
    private boolean h2ConsoleEnabled;

    @Value("${spring.h2.console.path:/h2-console}")
    private String h2ConsolePath;

    @Value("${spring.h2.console.settings.web-allow-others:true}")
    private boolean h2ConsoleWebAllowOthers;

    // JPA配置
    @Value("${spring.jpa.hibernate.ddl-auto:create-drop}")
    private String jpaHibernateDdlAuto;

    @Value("${spring.jpa.show-sql:true}")
    private boolean jpaShowSql;

    @Value("${spring.main.allow-bean-definition-overriding:false}")
    private boolean allowBeanDefinitionOverriding;

    // 服务器配置
    @Value("${server.port:8080}")
    private int serverPort;

    // 日志配置
    @Value("${logging.level.root:INFO}")
    private String loggingLevelRoot;

    @Value("${logging.level.com.calendar.chart:DEBUG}")
    private String loggingLevelCalendarChart;

    @Value("${logging.level.org.springframework.web:INFO}")
    private String loggingLevelSpringWeb;

    @Value("${logging.level.org.hibernate:INFO}")
    private String loggingLevelHibernate;

    // MyBatis-Plus配置
    @Value("${mybatis-plus.mapper-locations:classpath:mapper/*.xml}")
    private String mybatisPlusMapperLocations;

    @Value("${mybatis-plus.type-aliases-package:com.calendar.chart.entity}")
    private String mybatisPlusTypeAliasesPackage;

    @Value("${mybatis-plus.configuration.map-underscore-to-camel-case:true}")
    private boolean mybatisPlusMapUnderscoreToCamelCase;

    @Value("${mybatis-plus.configuration.log-impl:org.apache.ibatis.logging.stdout.StdOutImpl}")
    private String mybatisPlusLogImpl;

    // LangChain4j OpenAI配置
    @Value("${langchain4j.open-ai.chat-model.api-key:demo}")
    private String langchain4jApiKey;

    @Value("${langchain4j.open-ai.chat-model.model-name:gpt-4o-mini}")
    private String langchain4jModelName;

    @Value("${langchain4j.open-ai.chat-model.thinking-model-name:gpt-4o-mini}")
    private String langchain4jThinkingModelName;

    @Value("${langchain4j.open-ai.chat-model.base-url:http://langchain4j.dev/demo/openai/v1}")
    private String langchain4jBaseUrl;

    @Value("${langchain4j.open-ai.chat-model.log-requests:false}")
    private boolean langchain4jLogRequests;

    @Value("${langchain4j.open-ai.chat-model.log-responses:false}")
    private boolean langchain4jLogResponses;

    // LangChain4j OpenAI配置
    @Value("${langchain4j.bai-lian.api-key:demo}")
    private String baiLianApiKey;

    @Value("${langchain4j.bai-lian.model-name:qwen-plus}")
    private String baiLianModelName;

    @Value("${langchain4j.bai-lian.base-url:https://dashscope.aliyuncs.com/compatible-mode/v1}")
    private String baiLianBaseUrl;

    @Value("${spring.ai.dashscope.api-key}")
    private String apiKey;

    @Value("${spring.skills.path}")
    private String skillsPath;

}