# Spring AI Application

这是一个基于Spring AI Alibaba框架构建的应用程序，用于演示AI功能集成。

## 项目特性

- 集成阿里云百炼平台
- 支持多种AI模型调用
- 包含工具调用功能
- 提供图式AI工作流支持
- 配置了预定义的AI Agent和工具
- 集成MCP（Model Context Protocol）客户端

## 环境要求

- Java 21+
- Maven 3.8+
- Node.js 和 npm（用于MCP Excel服务器）

## 快速开始

1. 设置环境变量：
```bash
export BAI_LIAN_API_KEY=your_api_key_here
```

2. 安装Node.js依赖（用于MCP服务）：
```bash
npm install -g @negokaz/excel-mcp-server
```

3. 构建项目：
```bash
./mvnw clean install
```

4. 运行应用程序：
```bash
./mvnw spring-boot:run
```

## API端点

- `GET http://localhost:7341/hello` - 基础测试端点
- `GET http://localhost:7341/ai/simple-chat?message=your_message` - 简单AI聊天
- `GET http://localhost:7341/ai/prompt-chat?message=your_message` - 带提示词的AI聊天
- `GET http://localhost:7341/test/chat?message=your_message` - 测试AI对话接口
- `GET http://localhost:7341/test/chat/react?message=your_message` - React Agent测试
- `GET http://localhost:7341/test/chat/react/tools?message=your_message` - 工具调用Agent测试
- `GET http://localhost:7341/mcp/status` - MCP服务状态
- `GET http://localhost:7341/mcp/clients` - MCP客户端数量

## MCP（Model Context Protocol）客户端

项目集成了Spring AI的MCP（Model Context Protocol）客户端，提供对外部资源的访问能力：

### MCP配置
- 配置文件：`src/main/resources/mcp-servers.json`
- Excel MCP服务器：`@negokaz/excel-mcp-server`
- 单页单元格限制：4000个单元格

### MCP客户端功能
- 自动连接Excel MCP服务器
- 提供Excel文件访问能力
- 通过标准MCP协议与外部服务器通信
- 支持同步客户端操作
- 集成到Spring AI工具执行框架
- 可在agent中使用MCP工具

### MCP服务管理API
- `/mcp/status` - 获取MCP服务可用状态
- `/mcp/clients` - 获取MCP客户端数量

### MCP工具测试API
- `/mcp/test/chat?message=your_message` - 测试MCP工具在聊天客户端中的使用
- `/mcp/test/agent?message=your_message` - 测试MCP工具在ReactAgent中的使用

### 环境要求
- 需要全局安装 `@negokaz/excel-mcp-server` 包
- 需要Node.js和npm环境

## 配置说明

- 应用端口：默认为 7341
- API密钥：通过环境变量 BAI_LIAN_API_KEY 配置
- MCP配置：在 application.yml 中设置
- 模型配置：在 application.yml 中设置
- 模型相关配置位于 `src/main/java/com/example/springaiapp/config/` 目录
- 工具配置位于 `src/main/java/com/example/springaiapp/tools/` 目录
- MCP相关配置位于 `src/main/java/com/example/springaiapp/mcp/` 目录

## 模型配置

项目包含以下模型配置组件：
- `LLMConfig.java`: LLM模型配置，包括React Agent、ChatClient等
- `ApplicationConfig.java`: 应用程序配置，管理API密钥等
- `ToolsConfig.java`: 工具配置，注册可调用的工具
- `ToolErrorInterceptor.java`: 工具调用错误处理

## 工具配置

项目包含以下内置工具：
- `WeatherTool.java`: 天气查询工具
- `TimeTool.java`: 时间查询工具
- `UserLocationTool.java`: 用户位置获取工具

## 目录结构

```
src/
├── main/
│   ├── java/com/example/springaiapp/
│   │   ├── TaoRunHuiApplication.java
│   │   ├── config/          # 模型和应用配置
│   │   ├── controller/      # 控制器
│   │   ├── mcp/             # MCP客户端配置
│   │   ├── skills/          # AI技能
│   │   ├── service/         # 服务层
│   │   ├── tools/           # 工具类
│   │   └── example/         # 示例代码
│   └── resources/
│       ├── application.properties
│       ├── application.yml
│       └── mcp-servers.json # MCP服务器配置
├── skills/                  # 外部技能目录
│   └── purchase-contract-generator/ # 采购合同生成技能
└── generated_contracts/     # 生成的合同文件目录（运行时创建）
```

2. 构建项目：
```bash
./mvnw clean install
```

3. 运行应用程序：
```bash
./mvnw spring-boot:run
```

## API端点

- `GET http://localhost:7341/hello` - 基础测试端点
- `GET http://localhost:7341/ai/simple-chat?message=your_message` - 简单AI聊天
- `GET http://localhost:7341/ai/prompt-chat?message=your_message` - 带提示词的AI聊天
- `GET http://localhost:7341/test/chat?message=your_message` - 测试AI对话接口
- `GET http://localhost:7341/test/chat/react?message=your_message` - React Agent测试
- `GET http://localhost:7341/test/chat/react/tools?message=your_message` - 工具调用Agent测试

## MCP（Model Context Protocol）配置

项目包含MCP服务器配置，用于处理Excel文件：
- 配置文件：`.ai-context.json`
- Excel MCP服务器：`@negokaz/excel-mcp-server`
- 单页单元格限制：4000个单元格

### MCP服务器启动

如果使用支持MCP的编辑器或IDE，Excel MCP服务器将在需要时自动启动。

## 配置说明

- 应用端口：默认为 7341
- API密钥：通过环境变量 BAI_LIAN_API_KEY 配置
- 模型配置：在 application.yml 中设置
- 模型相关配置位于 `src/main/java/com/example/springaiapp/config/` 目录
- 工具配置位于 `src/main/java/com/example/springaiapp/tools/` 目录

## 模型配置

项目包含以下模型配置组件：
- `LLMConfig.java`: LLM模型配置，包括React Agent、ChatClient等
- `ApplicationConfig.java`: 应用程序配置，管理API密钥等
- `ToolsConfig.java`: 工具配置，注册可调用的工具
- `ToolErrorInterceptor.java`: 工具调用错误处理

## 工具配置

项目包含以下内置工具：
- `WeatherTool.java`: 天气查询工具
- `TimeTool.java`: 时间查询工具
- `UserLocationTool.java`: 用户位置获取工具

## 目录结构

```
src/
├── main/
│   ├── java/com/example/springaiapp/
│   │   ├── SpringaiappApplication.java
│   │   ├── config/          # 模型和应用配置
│   │   ├── controller/      # 控制器
│   │   ├── service/         # 服务层
│   │   ├── tools/           # 工具类
│   │   └── example/         # 示例代码
│   └── resources/
│       ├── application.properties
│       └── application.yml
```