package com.example.springaiapp.config;

import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.client.transport.ServerParameters;
import jakarta.annotation.Resource;
import org.springframework.ai.mcp.SyncMcpToolCallbackProvider;
import org.springframework.context.annotation.Bean;

/**
 * @author Gabriel
 * @version 1.0
 * @date 2026/3/5 11:29
 * @description: TODO
 */
public class McpConfig {
    @Resource
    private SyncMcpToolCallbackProvider toolCallbackProvider;
}
