package com.example.springaiapp.config;

import com.alibaba.cloud.ai.graph.agent.hook.shelltool.ShellToolAgentHook;
import com.alibaba.cloud.ai.graph.agent.hook.skills.SkillsAgentHook;
import com.alibaba.cloud.ai.graph.agent.tools.ShellTool2;
import com.alibaba.cloud.ai.graph.skills.registry.SkillRegistry;
import com.alibaba.cloud.ai.graph.skills.registry.classpath.ClasspathSkillRegistry;
import com.alibaba.cloud.ai.graph.skills.registry.filesystem.FileSystemSkillRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

/**
 * @author Gabriel
 * @version 1.0
 * @date 2026/2/28 16:57
 * @description: TODO
 */
@Configuration
@RequiredArgsConstructor
public class SkillsConfig {


    private final ApplicationConfig applicationConfig;

    @Bean
    public SkillsAgentHook registerSkills() {
        /*SkillRegistry registry = FileSystemSkillRegistry.builder()
                .projectSkillsDirectory(System.getProperty("user.dir") + "/skills")
                .build();*/

        SkillRegistry registry = ClasspathSkillRegistry.builder()
                .classpathPath(applicationConfig.getSkillsPath())
                .build();

        // 3. Shell Hook：提供 Shell 命令执行（工作目录可指定，如当前工程目录）
       /* ShellToolAgentHook shellHook = ShellToolAgentHook.builder()
                .shellTool2(ShellTool2.builder(System.getProperty("./")).build())
                .build();*/

        return SkillsAgentHook.builder()
                .skillRegistry(registry)
                .autoReload(true)
                .build();
    }




}
