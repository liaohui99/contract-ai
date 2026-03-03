package com.example.springaiapp.config;

import com.alibaba.cloud.ai.agent.python.tool.PythonTool;
import lombok.extern.slf4j.Slf4j;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.io.IOAccess;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.function.FunctionToolCallback;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.File;
import java.util.function.BiFunction;

/**
 * Python工具配置类
 * 覆盖Spring AI Alibaba默认的Python工具配置，添加正确的GraalVM权限设置
 * 
 * @author Gabriel
 * @version 1.0
 */
@Slf4j
@Configuration
public class PythonToolConfig {

    @Value("${spring.skills.path:src/main/resources/skills}")
    private String skillsPath;

    /**
     * 创建自定义的Python工具执行器
     * 配置完整的GraalVM权限以解决PermissionError问题
     */
    //@Bean
    //@ConditionalOnMissingBean
    public BiFunction<PythonTool.PythonRequest, ToolContext, String> pythonToolExecutor() {
        return (request, context) -> {
            String pythonCode = request.code;
            log.debug("执行Python代码，长度: {}", pythonCode.length());

            String workingDir = System.getProperty("user.dir");
            String pythonPath = skillsPath + File.pathSeparator + workingDir;

            try (Context polyglotContext = Context.newBuilder("python")
                    .allowAllAccess(true)
                    .allowIO(IOAccess.ALL)
                    .allowNativeAccess(true)
                    .allowCreateThread(true)
                    .allowHostClassLoading(true)
                    .allowHostClassLookup(className -> true)
                    .allowCreateProcess(true)
                    .option("python.WarnInterpreterOnly", "false")
                    .option("python.PosixModuleBackend", "native")
                    .option("python.PythonPath", pythonPath)
                    .build()) {

                var result = polyglotContext.eval("python", pythonCode);
                String resultStr = result != null ? result.toString() : "null";
                log.debug("Python执行成功，结果长度: {}", resultStr.length());
                return resultStr;
            } catch (org.graalvm.polyglot.PolyglotException e) {
                log.error("Python执行失败: {}", e.getMessage(), e);
                return "Python执行失败: " + e.getMessage();
            } catch (Exception e) {
                log.error("系统错误: {}", e.getMessage(), e);
                return "系统错误: " + e.getMessage();
            }
        };
    }

    /**
     * 创建Python工具回调
     */
    //@Bean
    //@ConditionalOnMissingBean(name = "pythonToolCallback")
    public ToolCallback pythonToolCallback(BiFunction<PythonTool.PythonRequest, ToolContext, String> pythonToolExecutor) {
        return FunctionToolCallback.builder("python", pythonToolExecutor)
                .description("Execute Python code and return the result. Use this tool to run Python scripts for data processing, file operations, or other computational tasks.")
                .inputType(PythonTool.PythonRequest.class)
                .build();
    }
}
