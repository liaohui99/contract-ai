# GraalVM Python 权限错误解决方案

## 问题描述

在使用 GraalVM Polyglot 执行 Python 代码时，遇到以下权限错误：

```
org.graalvm.polyglot.PolyglotException: PermissionError: tuple(ObjectSequenceStorage[1, Operation not permitted])
```

## 根本原因分析

### 1. GraalVM Python 版本限制

项目当前使用的 GraalVM Python 版本为 **24.1.2**，该版本对 Python 3.9+ 的类型注解语法支持不完整。

### 2. 类型注解语法不兼容

Skills 目录中的许多 Python 文件使用了 Python 3.9+ 的类型注解语法：

```python
# Python 3.9+ 新语法（GraalVM Python 不支持）
def example() -> tuple[int, int]:
    return (1, 2)

def example2(color: tuple[int, int, int] = (255, 255, 255)):
    pass
```

GraalVM Python 内部使用 `ObjectSequenceStorage` 来存储对象，当遇到不支持的类型注解时会抛出 `PermissionError`。

### 3. 受影响的文件

以下文件包含不兼容的类型注解语法：

- `skills/slack-gif-creator/core/validators.py`
- `skills/slack-gif-creator/core/frame_composer.py`
- `skills/slack-gif-creator/core/easing.py`
- `skills/skill-creator/scripts/utils.py`
- `skills/skill-creator/scripts/run_loop.py`
- `skills/skill-creator/scripts/generate_report.py`
- `skills/pptx/scripts/office/helpers/simplify_redlines.py`
- `skills/pptx/scripts/office/helpers/merge_runs.py`
- `skills/pptx/scripts/office/unpack.py`
- `skills/pptx/scripts/office/pack.py`
- `skills/pptx/scripts/thumbnail.py`
- `skills/pptx/scripts/add_slide.py`
- `skills/mcp-builder/scripts/evaluation.py`
- `skills/docx/scripts/office/helpers/simplify_redlines.py`
- `skills/docx/scripts/office/helpers/merge_runs.py`
- `skills/docx/scripts/office/unpack.py`
- `skills/docx/scripts/office/pack.py`
- `skills/docx/scripts/comment.py`
- `skills/docx/scripts/accept_changes.py`
- `skills/xlsx/scripts/office/helpers/simplify_redlines.py`
- `skills/xlsx/scripts/office/helpers/merge_runs.py`
- `skills/xlsx/scripts/office/unpack.py`
- `skills/xlsx/scripts/office/pack.py`

## 解决方案

### 方案一：升级 GraalVM Python（推荐）

#### 步骤 1：升级依赖版本

修改 `pom.xml` 文件，升级 GraalVM Python 到最新版本：

```xml
<properties>
    <graalvm.version>24.2.1</graalvm.version>
</properties>

<dependencies>
    <dependency>
        <groupId>org.graalvm.polyglot</groupId>
        <artifactId>polyglot</artifactId>
        <version>${graalvm.version}</version>
    </dependency>
    <dependency>
        <groupId>org.graalvm.polyglot</groupId>
        <artifactId>python</artifactId>
        <version>${graalvm.version}</version>
        <type>pom</type>
    </dependency>
</dependencies>
```

#### 步骤 2：下载并安装 GraalVM

1. 访问 [GraalVM 官网](https://www.graalvm.org/downloads/) 下载最新版本
2. 解压到本地目录，例如：`E:\JDK\graalvm-ce-java21-24.2.1`
3. 设置环境变量：

```powershell
# 临时设置（PowerShell）
$env:GRAALVM_HOME = "E:\JDK\graalvm-ce-java21-24.2.1"
$env:PATH = "$env:GRAALVM_HOME\bin;$env:PATH"

# 永久设置（系统环境变量）
GRAALVM_HOME = E:\JDK\graalvm-ce-java21-24.2.1
Path 添加 = %GRAALVM_HOME%\bin
```

#### 步骤 3：验证安装

```powershell
# 检查 GraalVM 版本
graalpy --version

# 检查 Python 版本
graalpy --version

# 测试类型注解
graalpy -c "def test() -> tuple[int, int]: return (1, 2); print(test())"
```

### 方案二：修改 Python 代码以兼容 GraalVM Python

如果无法升级 GraalVM Python，需要修改所有受影响的 Python 文件，将类型注解改为兼容语法。

#### 修改规则

```python
# ❌ 不兼容的写法（Python 3.9+）
def example() -> tuple[int, int]:
    return (1, 2)

def example2(color: tuple[int, int, int] = (255, 255, 255)):
    pass

# ✅ 兼容的写法（Python 3.7+）
from typing import Tuple

def example() -> Tuple[int, int]:
    return (1, 2)

def example2(color: Tuple[int, int, int] = (255, 255, 255)):
    pass
```

#### 批量修改脚本

创建一个 Python 脚本来自动修改所有受影响的文件：

```python
import re
from pathlib import Path

def fix_type_annotations(file_path):
    """修复文件中的类型注解"""
    with open(file_path, 'r', encoding='utf-8') as f:
        content = f.read()
    
    original_content = content
    
    # 替换 tuple[int, ...] 为 Tuple[int, ...]
    content = re.sub(r'\btuple\[', r'Tuple[', content)
    
    # 添加 typing 导入（如果不存在）
    if 'Tuple[' in content and 'from typing import' not in content:
        # 在文件开头添加导入
        lines = content.split('\n')
        import_line = -1
        
        # 找到第一个 import 语句的位置
        for i, line in enumerate(lines):
            if line.strip().startswith('import ') or line.strip().startswith('from '):
                import_line = i
                break
        
        if import_line >= 0:
            lines.insert(import_line, 'from typing import Tuple')
        else:
            lines.insert(0, 'from typing import Tuple')
        
        content = '\n'.join(lines)
    
    # 如果内容有变化，写回文件
    if content != original_content:
        with open(file_path, 'w', encoding='utf-8') as f:
            f.write(content)
        print(f"已修复: {file_path}")
        return True
    return False

def fix_all_files(skills_dir):
    """修复所有 Python 文件"""
    skills_path = Path(skills_dir)
    fixed_count = 0
    
    for py_file in skills_path.rglob('*.py'):
        if fix_type_annotations(py_file):
            fixed_count += 1
    
    print(f"\n共修复 {fixed_count} 个文件")

if __name__ == '__main__':
    skills_directory = 'src/main/resources/skills'
    fix_all_files(skills_directory)
```

### 方案三：配置 GraalVM Python 权限

在 Java 代码中配置 GraalVM Python 的权限设置：

```java
import org.graalvm.polyglot.*;

// 创建 Context 时配置权限
Context context = Context.newBuilder("python")
    .allowAllAccess(true)  // 允许所有访问（谨慎使用）
    .allowIO(true)         // 允许 IO 操作
    .allowNativeAccess(true)  // 允许本地访问
    .allowCreateThread(true)  // 允许创建线程
    .allowHostClassLoading(true)  // 允许加载主机类
    .allowHostClassLookup((className) -> true)  // 允许查找主机类
    .build();
```

### 方案四：使用标准 Python 替代 GraalVM Python

如果不需要多语言互操作，可以考虑使用标准 Python 替代 GraalVM Python。

#### 步骤 1：移除 GraalVM 依赖

从 `pom.xml` 中移除 GraalVM 依赖：

```xml
<!-- 移除以下依赖 -->
<!--
<dependency>
    <groupId>org.graalvm.polyglot</groupId>
    <artifactId>polyglot</artifactId>
    <version>24.1.2</version>
</dependency>
<dependency>
    <groupId>org.graalvm.polyglot</groupId>
    <artifactId>python</artifactId>
    <version>24.1.2</version>
    <type>pom</type>
</dependency>
-->
```

#### 步骤 2：使用 ProcessBuilder 调用 Python

```java
import java.io.BufferedReader;
import java.io.InputStreamReader;

public class PythonExecutor {
    
    public String executePython(String code) throws Exception {
        ProcessBuilder processBuilder = new ProcessBuilder();
        processBuilder.command("python", "-c", code);
        processBuilder.redirectErrorStream(true);
        
        Process process = processBuilder.start();
        
        StringBuilder output = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }
        }
        
        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new RuntimeException("Python 执行失败: " + output);
        }
        
        return output.toString();
    }
}
```

## 推荐方案

根据项目需求，推荐使用 **方案一：升级 GraalVM Python**，原因如下：

1. **长期解决方案**：升级到最新版本可以获得更好的兼容性和性能
2. **无需修改代码**：Skills 中的 Python 代码无需修改
3. **官方支持**：新版本会修复已知问题并提供更好的支持

如果升级不可行，则使用 **方案二：修改 Python 代码**，这是最稳妥的临时解决方案。

## 验证步骤

### 1. 验证 GraalVM Python 版本

```powershell
graalpy --version
```

### 2. 测试类型注解

```powershell
graalpy -c "from typing import Tuple; def test() -> Tuple[int, int]: return (1, 2); print(test())"
```

### 3. 测试 Skills 功能

启动应用后，调用包含类型注解的技能，确认不再出现权限错误。

## 常见问题

### Q1: 升级后仍然出现权限错误

**解决方法**：
1. 确认 GraalVM Python 版本已正确升级
2. 清理 Maven 缓存：`mvn clean`
3. 重新构建项目：`mvn package`
4. 重启应用

### Q2: 批量修改脚本执行失败

**解决方法**：
1. 检查文件路径是否正确
2. 确认有写入权限
3. 手动修改关键文件

### Q3: 使用标准 Python 后性能下降

**解决方法**：
1. 使用缓存机制减少 Python 调用次数
2. 考虑使用 Jython（如果只需要 Python 2.x）
3. 优化 Python 代码性能

## 参考资料

- [GraalVM Python 文档](https://www.graalvm.org/python/)
- [GraalVM Polyglot API](https://www.graalvm.org/sdk/javadoc/org/graalvm/polyglot/package-summary.html)
- [Python 类型注解](https://docs.python.org/zh-cn/3/library/typing.html)
- [GraalVM 发布说明](https://www.graalvm.org/release-notes/)
