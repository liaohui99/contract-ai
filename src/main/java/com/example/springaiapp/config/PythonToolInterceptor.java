package com.example.springaiapp.config;

import com.alibaba.cloud.ai.graph.agent.interceptor.ToolCallHandler;
import com.alibaba.cloud.ai.graph.agent.interceptor.ToolCallRequest;
import com.alibaba.cloud.ai.graph.agent.interceptor.ToolCallResponse;
import com.alibaba.cloud.ai.graph.agent.interceptor.ToolInterceptor;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;

import java.io.File;

/**
 * Python工具拦截器
 * 用于预处理Python代码，解决GraalVM环境中__file__变量未定义的问题
 * 同时注入skills路径，解决第三方库导入问题
 * @author Gabriel
 * @version 1.0
 */
@Slf4j
public class PythonToolInterceptor extends ToolInterceptor {

    private static final String PYTHON_TOOL_NAME = "python";
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    
    private final String skillsPath;

    public PythonToolInterceptor(String skillsPath) {
        this.skillsPath = skillsPath;
    }

    @Override
    public ToolCallResponse interceptToolCall(ToolCallRequest request, ToolCallHandler handler) {
        if (!request.getToolName().contains(PYTHON_TOOL_NAME)) {
            return handler.call(request);
        }

        try {
            ToolCallRequest modifiedRequest = new ToolCallRequest(
                    request.getToolName(),
                    request.getArguments(),
                    request.getToolCallId(),
                    request.getContext(),
                    request.getExecutionContext().get()
            );
            //log.debug("Python code preprocessed to inject __file__ variable and sys.path");
            return handler.call(modifiedRequest);
        } catch (Exception e) {
            log.warn("Failed to preprocess Python code: {}", e.getMessage());
            return handler.call(request);
        }
    }

    /**
     * 预处理Python代码参数，注入__file__变量和sys.path
     */
    private String preprocessPythonArgs(String args) {
        try {
            JsonNode argsNode = OBJECT_MAPPER.readTree(args);
            JsonNode codeNode = argsNode.get("code");
            
            if (codeNode != null && codeNode.isTextual()) {
                String originalCode = codeNode.asText();
                String modifiedCode = injectEnvironmentSetup(originalCode);
                
                if (!originalCode.equals(modifiedCode)) {
                    ((ObjectNode) argsNode).put("code", modifiedCode);
                    log.debug("Injected __file__ variable and sys.path into Python code");
                }
            }
            
            return OBJECT_MAPPER.writeValueAsString(argsNode);
        } catch (Exception e) {
            log.warn("Failed to parse Python tool args: {}", e.getMessage());
            return args;
        }
    }

    /**
     * 在Python代码中注入环境设置
     * 包括：__file__变量定义、sys.path设置、第三方库路径
     */
    private String injectEnvironmentSetup(String code) {
        if (code == null || code.isEmpty()) {
            return code;
        }

        String workingDir = System.getProperty("user.dir");
        String tempFilePath = workingDir + File.separator + "temp_script.py";
        String normalizedTempFilePath = tempFilePath.replace("\\", "\\\\");
        String normalizedSkillsPath = skillsPath.replace("\\", "\\\\");
        String normalizedWorkingDir = workingDir.replace("\\", "\\\\");
        
        StringBuilder setupCode = new StringBuilder();
        
        setupCode.append("import sys\n");
        setupCode.append("import os\n");
        setupCode.append("\n");
        setupCode.append("# GraalVM环境变量注入\n");
        setupCode.append("if '__file__' not in dir() or not __file__:\n");
        setupCode.append(String.format("    __file__ = r'%s'\n", normalizedTempFilePath));
        setupCode.append("\n");
        setupCode.append("# 设置sys.path以支持第三方库和skills导入\n");
        setupCode.append(String.format("_skills_path = r'%s'\n", normalizedSkillsPath));
        setupCode.append(String.format("_working_dir = r'%s'\n", normalizedWorkingDir));
        setupCode.append("if _skills_path not in sys.path:\n");
        setupCode.append("    sys.path.insert(0, _skills_path)\n");
        setupCode.append("if _working_dir not in sys.path:\n");
        setupCode.append("    sys.path.insert(0, _working_dir)\n");
        setupCode.append("\n");
        setupCode.append("# 设置环境变量\n");
        setupCode.append("os.environ['SKILLS_PATH'] = _skills_path\n");
        setupCode.append("os.environ['PYTHONPATH'] = os.environ.get('PYTHONPATH', '') + os.pathsep + _skills_path\n");
        setupCode.append("\n");
        
        String setupStr = setupCode.toString();
        
        if (code.contains("__file__") || code.contains("sys.path") || 
            code.contains("from xlsx_template_processor") || code.contains("import xlsx_template_processor")) {
            return setupStr + "\n" + code;
        }
        
        return code;
    }

    @Override
    public String getName() {
        return "PythonToolInterceptor";
    }
}
