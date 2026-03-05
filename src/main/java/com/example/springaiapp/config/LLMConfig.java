package com.example.springaiapp.config;

import com.alibaba.cloud.ai.agent.python.tool.PythonTool;
import com.alibaba.cloud.ai.dashscope.api.DashScopeApi;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatModel;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import com.alibaba.cloud.ai.graph.agent.hook.shelltool.ShellToolAgentHook;
import com.alibaba.cloud.ai.graph.agent.hook.skills.SkillsAgentHook;
import com.alibaba.cloud.ai.graph.agent.tools.ShellTool2;
import com.alibaba.cloud.ai.graph.checkpoint.savers.MemorySaver;
import com.example.springaiapp.skills.CustomPythonTool;
import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.spec.McpSchema;
import io.modelcontextprotocol.spec.McpServerTransportProviderBase;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.mcp.SyncMcpToolCallbackProvider;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Gabriel
 * @version 1.0
 * @date 2026/2/9 17:06
 * @description: TODO
 */
@Configuration
@RequiredArgsConstructor
public class LLMConfig {

    private final ApplicationConfig applicationConfig;
    private final SyncMcpToolCallbackProvider toolCallbackProvider;


    @Bean
    public ReactAgent contractReactAgent(ChatModel chatModel,
                                         SkillsAgentHook registerSkills,
                                         List<ToolCallback> toolCallbacks
    ) {

        // 3. Shell Hook：提供 Shell 命令执行（工作目录可指定，如当前工程目录）
        ShellToolAgentHook shellHook = ShellToolAgentHook.builder()
                .shellTool2(ShellTool2.builder(System.getProperty("user.dir")).build())
                .build();
        // PythonTool 已通过 spring-ai-alibaba-starter-tool-calling-python 自动配置，无需手动添加
        //toolCallbacks.add(PythonTool.createPythonToolCallback(PythonTool.DESCRIPTION+" \n 如缺失相关依赖，例如No module named 'openpyxl'，请自行安装"));
        //toolCallbacks.add(CustomPythonTool.createPythonToolCallback(PythonTool.DESCRIPTION + " \n 如缺失相关依赖，例如No module named 'openpyxl'，请自行安装"));
        toolCallbacks.addAll(List.of(toolCallbackProvider.getToolCallbacks()));
        // 创建Python工具拦截器，解决GraalVM环境中__file__变量未定义的问题
        PythonToolInterceptor pythonInterceptor = new PythonToolInterceptor(
                applicationConfig.getSkillsPath()
        );

        return ReactAgent.builder()
                .name("contract_agent")
                .model(chatModel)
                .tools(toolCallbacks)
                .saver(new MemorySaver())
                .interceptors(pythonInterceptor)
                .hooks(List.of(registerSkills, shellHook))
                .systemPrompt(""" 
                        你是一个合同生成助手，可根据用户需求修改或生成合同，如果生成新合同，请参考template-parser技能中定义的合同规范；
                        合同相关的需求不明确可向用户提问，可调用excel-read、excel-write、contract-generator、output_with_docxpdf等工具处理合同文件；
                        默认使用中文回答;
                        生成的合同默认保存至D:\\tmp目录下
                        """
                )
                .build();
    }


    @Bean
    public ReactAgent faruiReactAgent(ChatModel chatModel) {
        // 将 MCP 客户端的所有工具转换为 FunctionCallback 列表
//        List<McpSchema.Tool> collect = mcpClients.stream()
//                .flatMap(client -> client.listTools().tools().stream())
//                .collect(Collectors.toList());
        PythonToolInterceptor pythonInterceptor = new PythonToolInterceptor(
                applicationConfig.getSkillsPath()
        );
        return ReactAgent.builder()
                .name("contract_agent")
                .model(chatModel)
                .tools(toolCallbackProvider.getToolCallbacks())
                .interceptors(pythonInterceptor)
                .saver(new MemorySaver())
                .systemPrompt(""" 
                        你是一个法律的专家，可根据用户的用户的问题，回答相关法律法规问题，回答的答案必须基于事实且引用真实的参考资料。
                        """
                )
                .build();
    }


    //@Bean
    public ChatModel myDashScopeChatModel() {
        DashScopeApi dashScopeApi = DashScopeApi.builder()
                .apiKey(applicationConfig.getApiKey())
                .build();

        return DashScopeChatModel.builder()
                .dashScopeApi(dashScopeApi)
                .defaultOptions(DashScopeChatOptions.builder()
                        // Note: model must be set when use options build.
                        .withModel(DashScopeChatModel.DEFAULT_MODEL_NAME)
                        .withTemperature(0.7)    // 控制随机性
                        .withMaxToken(2000)      // 最大输出长度
                        .withTopP(0.9)           // 核采样参数
                        .withTemperature(0.5)   // 随机性
                        .build())
                .build();

    }


    @Bean
    public ChatClient.Builder chatClientBuilder(ChatModel chatModel) {
        return ChatClient.builder(chatModel);
    }

    /**
     * 创建一个包含MCP工具的聊天客户端
     */
    @Bean
    public ChatClient chatClientWithMcpTools(ChatModel chatModel, List<ToolCallback> toolCallbacks) {
        return ChatClient.builder(chatModel)
                .defaultToolCallbacks(toolCallbacks)  // 包含本地工具和MCP工具
                .build();
    }

}