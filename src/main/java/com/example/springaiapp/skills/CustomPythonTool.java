package com.example.springaiapp.skills;

import com.alibaba.cloud.ai.agent.python.tool.PythonTool;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.function.FunctionToolCallback;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;

/**
 * 自定义Python工具
 * 通过Shell执行系统Python，解决GraalVM Python环境隔离导致的依赖问题
 * 支持自动检查和安装Python依赖
 *
 * @author Gabriel
 * @version 2.0
 */
@Slf4j
@Component
public class CustomPythonTool implements BiFunction<PythonTool.PythonRequest, ToolContext, String> {

    private static final String DEFAULT_SKILLS_PATH = "src/main/resources/skills";

    private static final Set<String> REQUIRED_PACKAGES = new HashSet<>(Arrays.asList(
            "openpyxl", "pandas", "requests", "numpy", "xlrd", "xlwt"
    ));

    private static final int EXECUTION_TIMEOUT_SECONDS = 60;

    private volatile boolean dependenciesChecked = false;

    private String pythonExecutable;

    /**
     * 应用启动时检查并安装依赖
     */
    @PostConstruct
    public void init() {
        this.pythonExecutable = detectPythonExecutable();
        log.info("检测到Python解释器: {}", pythonExecutable);
        checkAndInstallDependencies();
    }

    /**
     * 检测系统Python解释器路径
     */
    private String detectPythonExecutable() {
        String[] candidates = {"python", "python3", "python.exe", "python3.exe"};

        for (String candidate : candidates) {
            try {
                ProcessBuilder pb = new ProcessBuilder(candidate, "--version");
                pb.redirectErrorStream(true);
                Process process = pb.start();
                boolean finished = process.waitFor(5, TimeUnit.SECONDS);

                if (finished && process.exitValue() == 0) {
                    try (BufferedReader reader = new BufferedReader(
                            new InputStreamReader(process.getInputStream()))) {
                        String version = reader.readLine();
                        log.info("找到Python: {} - {}", candidate, version);
                    }
                    return candidate;
                }
            } catch (Exception e) {
                log.debug("检测 {} 失败: {}", candidate, e.getMessage());
            }
        }

        log.warn("未找到系统Python，将使用默认 'python'");
        return "python";
    }

    /**
     * 检查并安装所需的Python依赖
     */
    private synchronized void checkAndInstallDependencies() {
        if (dependenciesChecked) {
            return;
        }

        log.info("检查Python依赖...");

        try {
            Set<String> missingPackages = checkMissingPackages();

            if (!missingPackages.isEmpty()) {
                log.info("缺少以下依赖包: {}", missingPackages);
                installPackages(missingPackages);
            } else {
                log.info("所有Python依赖已安装");
            }

            dependenciesChecked = true;
        } catch (Exception e) {
            log.error("检查Python依赖失败: {}", e.getMessage());
        }
    }

    /**
     * 检查缺失的Python包
     */
    private Set<String> checkMissingPackages() {
        Set<String> missing = new HashSet<>();

        for (String pkg : REQUIRED_PACKAGES) {
            try {
                ProcessBuilder pb = new ProcessBuilder(
                        pythonExecutable, "-c", "import " + pkg.replace("-", "_")
                );
                pb.redirectErrorStream(true);
                Process process = pb.start();
                int exitCode = process.waitFor();

                if (exitCode != 0) {
                    missing.add(pkg);
                }
            } catch (Exception e) {
                missing.add(pkg);
                log.debug("检查包 {} 失败: {}", pkg, e.getMessage());
            }
        }

        return missing;
    }

    /**
     * 安装缺失的Python包
     */
    private void installPackages(Set<String> packages) {
        for (String pkg : packages) {
            try {
                log.info("正在安装: {} ...", pkg);
                ProcessBuilder pb = new ProcessBuilder(
                        pythonExecutable, "-m", "pip", "install", pkg
                );
                pb.redirectErrorStream(true);
                Process process = pb.start();

                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(process.getInputStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        log.debug("pip: {}", line);
                    }
                }

                int exitCode = process.waitFor();
                if (exitCode == 0) {
                    log.info("成功安装: {}", pkg);
                } else {
                    log.warn("安装失败: {}", pkg);
                }
            } catch (Exception e) {
                log.error("安装 {} 失败: {}", pkg, e.getMessage());
            }
        }
    }

    /**
     * 执行Python代码
     * 通过将代码写入临时文件并使用系统Python执行
     */
    @Override
    public String apply(PythonTool.PythonRequest request, ToolContext context) {
        ensureInitialized();

        String pythonCode = request.code;
        log.debug("执行Python代码，长度: {}", pythonCode.length());

        String workingDir = System.getProperty("user.dir");
        String skillsPath = System.getProperty("spring.skills.path", DEFAULT_SKILLS_PATH);

        Path tempFile = null;
        try {
            tempFile = createTempPythonFile(pythonCode, skillsPath, workingDir);

            String result = executePythonScript(tempFile.toFile(), workingDir);
            log.debug("Python执行成功，结果长度: {}", result.length());
            return result;
        } catch (Exception e) {
            log.error("Python执行失败: {}", e.getMessage(), e);
            return "Python执行失败: " + e.getMessage();
        } finally {
            if (tempFile != null) {
                try {
                    Files.deleteIfExists(tempFile);
                } catch (IOException e) {
                    log.debug("删除临时文件失败: {}", e.getMessage());
                }
            }
        }
    }

    /**
     * 确保实例已初始化
     * 支持非Spring管理的实例也能正常工作
     */
    private synchronized void ensureInitialized() {
        if (pythonExecutable == null) {
            this.pythonExecutable = detectPythonExecutable();
            log.info("延迟初始化Python解释器: {}", pythonExecutable);
            checkAndInstallDependencies();
        }
    }

    /**
     * 创建临时Python文件，包含必要的路径设置
     */
    private Path createTempPythonFile(String pythonCode, String skillsPath, String workingDir) throws IOException {
        Path tempFile = Files.createTempFile("python_script_", ".py");

        StringBuilder fullCode = new StringBuilder();
        fullCode.append("# -*- coding: utf-8 -*-\n");
        fullCode.append("import sys\n");
        fullCode.append("import os\n\n");

        fullCode.append("sys.path.insert(0, '").append(skillsPath.replace("\\", "/")).append("')\n");
        fullCode.append("sys.path.insert(0, '").append(workingDir.replace("\\", "/")).append("')\n");
        fullCode.append("os.chdir('").append(workingDir.replace("\\", "/")).append("')\n\n");

        fullCode.append(pythonCode);

        Files.writeString(tempFile, fullCode.toString(), StandardCharsets.UTF_8);
        return tempFile;
    }

    /**
     * 执行Python脚本并返回输出
     */
    private String executePythonScript(File scriptFile, String workingDir) throws Exception {
        ProcessBuilder pb = new ProcessBuilder(pythonExecutable, scriptFile.getAbsolutePath());
        pb.directory(new File(workingDir));
        pb.redirectErrorStream(true);

        Process process = pb.start();

        StringBuilder output = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }
        }

        boolean finished = process.waitFor(EXECUTION_TIMEOUT_SECONDS, TimeUnit.SECONDS);
        if (!finished) {
            process.destroyForcibly();
            throw new RuntimeException("Python执行超时（" + EXECUTION_TIMEOUT_SECONDS + "秒）");
        }

        int exitCode = process.exitValue();
        if (exitCode != 0) {
            throw new RuntimeException("Python执行失败，退出码: " + exitCode + "\n输出: " + output);
        }

        return output.toString().trim();
    }

    /**
     * 工厂方法：将当前工具包装为FunctionToolCallback
     */
    public static ToolCallback createPythonToolCallback(String description) {
        return FunctionToolCallback.builder("python-new", new CustomPythonTool())
                .description(description)
                .inputType(PythonTool.PythonRequest.class)
                .build();
    }
}

