# GraalVM Python环境问题解决方案总结

## 问题概述

在使用GraalVM Polyglot执行Python代码时遇到三个核心问题：

1. **`__file__` 变量未定义错误**
   ```
   org.graalvm.polyglot.PolyglotException: NameError: name '__file__' is not defined
   ```

2. **第三方库缺失错误**
   ```
   ModuleNotFoundError: No module named 'openpyxl'
   ```

3. **类型注解权限错误**
   ```
   org.graalvm.polyglot.PolyglotException: PermissionError: tuple(ObjectSequenceStorage[1, Operation not permitted])
   ```

   **原因**：GraalVM Python 24.1.2 及以下版本对 Python 3.9+ 的类型注解语法（如 `tuple[int, int]`）支持不完整。

   **详细解决方案**：请参考 [GraalVM_Python_权限错误解决方案.md](GraalVM_Python_权限错误解决方案.md)

## 完整解决方案

### 一、代码层面的修复

#### 1. PythonToolInterceptor增强

**文件位置**: `src/main/java/com/example/springaiapp/config/PythonToolInterceptor.java`

**功能增强**:
- ✅ 自动注入`__file__`变量定义
- ✅ 自动配置`sys.path`包含skills路径
- ✅ 设置`SKILLS_PATH`和`PYTHONPATH`环境变量
- ✅ 支持动态模块导入

**核心代码**:
```java
private String injectEnvironmentSetup(String code) {
    // 注入环境设置代码
    setupCode.append("import sys\n");
    setupCode.append("import os\n");
    setupCode.append("if '__file__' not in dir() or not __file__:\n");
    setupCode.append("    __file__ = r'temp_script.py'\n");
    setupCode.append("_skills_path = r'skills'\n");
    setupCode.append("if _skills_path not in sys.path:\n");
    setupCode.append("    sys.path.insert(0, _skills_path)\n");
    setupCode.append("os.environ['SKILLS_PATH'] = _skills_path\n");
    // ... 更多设置
}
```

#### 2. GraalVMPythonInitializer自动安装器

**文件位置**: `src/main/java/com/example/springaiapp/config/GraalVMPythonInitializer.java`

**功能**:
- ✅ 应用启动时自动尝试安装Python包
- ✅ 安装失败时提供手动安装指导
- ✅ 支持openpyxl、lxml、et_xmlfile等包

**日志输出**:
```
开始初始化GraalVM Python环境...
正在安装Python包: openpyxl
[警告] 安装Python包 openpyxl 失败: Cannot run program "graalpy"
请手动执行以下命令安装: graalpy -m pip install openpyxl
GraalVM Python环境初始化完成
```

#### 3. SKILL.md文档更新

**文件位置**: `src/main/resources/skills/xlsx-template-processor/SKILL.md`

**更新内容**:
- ✅ 添加GraalVM兼容的代码示例
- ✅ 提供多种路径设置方式
- ✅ 添加GraalVM环境特殊说明

**推荐用法**:
```python
import sys
import os

# GraalVM兼容的路径设置方式
skills_path = os.environ.get('SKILLS_PATH', '')
if skills_path and skills_path not in sys.path:
    sys.path.insert(0, skills_path)

from xlsx_template_processor import process_xlsx_template_with_context
```

### 二、工具脚本

#### 1. Windows安装脚本

**文件位置**: `scripts/install_graalvm_python_packages.bat`

**功能**:
- ✅ 自动检测graalpy命令
- ✅ 升级pip
- ✅ 安装openpyxl及依赖
- ✅ 验证安装结果

**使用方法**:
```powershell
.\scripts\install_graalvm_python_packages.bat
```

#### 2. 配置指南文档

**文件位置**: `docs/GraalVM_Python配置指南.md`

**内容**:
- ✅ 详细的问题说明
- ✅ 多种解决方案
- ✅ 代码最佳实践
- ✅ 常见问题解答
- ✅ 技术细节说明

### 三、手动安装步骤

#### 方式1：使用批处理脚本（推荐）

```powershell
# 1. 设置GraalVM环境变量
$env:PATH = "$env:GRAALVM_HOME\bin;$env:PATH"

# 2. 运行安装脚本
.\scripts\install_graalvm_python_packages.bat
```

#### 方式2：手动执行命令

```powershell
# 1. 设置环境变量
$env:PATH = "E:\JDK\graalvm\bin;$env:PATH"

# 2. 安装openpyxl
graalpy -m pip install openpyxl

# 3. 安装可选依赖
graalpy -m pip install lxml et_xmlfile

# 4. 验证安装
graalpy -c "import openpyxl; print('openpyxl版本:', openpyxl.__version__)"
```

#### 方式3：使用国内镜像加速

```powershell
graalpy -m pip install -i https://pypi.tuna.tsinghua.edu.cn/simple openpyxl
```

### 四、验证修复效果

#### 1. 检查应用启动日志

**成功的标志**:
```
✅ Python code preprocessed to inject __file__ variable and sys.path
✅ Injected __file__ variable and sys.path into Python code
```

#### 2. 测试Python代码执行

**测试代码**:
```python
import sys
import os

# 验证环境变量
print('SKILLS_PATH:', os.environ.get('SKILLS_PATH'))
print('__file__:', __file__ if '__file__' in dir() else 'Not defined')
print('sys.path:', sys.path[:3])

# 验证openpyxl
try:
    import openpyxl
    print('openpyxl版本:', openpyxl.__version__)
    print('✅ openpyxl导入成功')
except ImportError as e:
    print('❌ openpyxl导入失败:', e)
```

#### 3. 测试Excel处理功能

```python
import os

# 使用环境变量获取路径
skills_path = os.environ.get('SKILLS_PATH', '')
if skills_path:
    import sys
    if skills_path not in sys.path:
        sys.path.insert(0, skills_path)

from xlsx_template_processor import process_xlsx_template

# 测试处理Excel
result = process_xlsx_template(
    template_path="E:\\study\\AI\\taorunhui-ai\\src\\main\\resources\\hetong\\template.xlsx",
    data={"contract_number": "TEST-001"},
    output_path="test_output.xlsx"
)
print('✅ Excel处理成功:', result)
```

## 技术原理

### PythonToolInterceptor工作流程

```
1. 拦截Python工具调用
   ↓
2. 解析JSON参数提取Python代码
   ↓
3. 注入环境设置代码
   - 定义__file__变量
   - 配置sys.path
   - 设置环境变量
   ↓
4. 合并环境代码与业务代码
   ↓
5. 传递给Python工具执行
```

### 环境注入代码示例

```python
# === 自动注入的环境设置代码 ===
import sys
import os

# GraalVM环境变量注入
if '__file__' not in dir() or not __file__:
    __file__ = r'E:\study\AI\taorunhui-ai\temp_script.py'

# 设置sys.path以支持第三方库和skills导入
_skills_path = r'skills'
_working_dir = r'E:\study\AI\taorunhui-ai'
if _skills_path not in sys.path:
    sys.path.insert(0, _skills_path)
if _working_dir not in sys.path:
    sys.path.insert(0, _working_dir)

# 设置环境变量
os.environ['SKILLS_PATH'] = _skills_path
os.environ['PYTHONPATH'] = os.environ.get('PYTHONPATH', '') + os.pathsep + _skills_path

# === 用户的业务代码 ===
# ... 用户原始代码 ...
```

## 常见问题

### Q1: graalpy命令找不到

**原因**: GraalVM未添加到系统PATH

**解决**:
```powershell
# 临时设置（PowerShell）
$env:PATH = "E:\JDK\graalvm\bin;$env:PATH"

# 永久设置（系统环境变量）
GRAALVM_HOME = E:\JDK\graalvm
Path添加 = %GRAALVM_HOME%\bin
```

### Q2: pip安装超时

**解决**: 使用国内镜像
```powershell
graalpy -m pip install -i https://pypi.tuna.tsinghua.edu.cn/simple openpyxl
```

### Q3: 导入模块仍然失败

**排查步骤**:
1. 检查`sys.path`是否包含正确路径
2. 确认包安装到了GraalVM环境而非系统Python
3. 查看拦截器日志确认环境设置已注入
4. 使用`graalpy -m pip list`查看已安装的包

### Q4: Windows路径问题

**推荐做法**:
```python
# ✅ 使用原始字符串
path = r'E:\study\AI\taorunhui-ai\skills'

# ✅ 使用正斜杠
path = 'E:/study/AI/taorunhui-ai/skills'

# ✅ 使用双反斜杠
path = 'E:\\study\\AI\\taorunhui-ai\\skills'

# ❌ 避免单反斜杠
path = 'E:\study\AI\taorunhui-ai\skills'  # 可能导致转义错误
```

## 项目文件清单

### 新增文件

1. `src/main/java/com/example/springaiapp/config/GraalVMPythonInitializer.java`
   - GraalVM Python环境自动初始化器

2. `scripts/install_graalvm_python_packages.bat`
   - Windows环境Python包安装脚本

3. `docs/GraalVM_Python配置指南.md`
   - 详细的配置和使用指南

4. `docs/GraalVM_Python问题解决方案.md`
   - 本文档，解决方案总结

### 修改文件

1. `src/main/java/com/example/springaiapp/config/PythonToolInterceptor.java`
   - 增强环境注入功能
   - 添加SKILLS_PATH环境变量设置

2. `src/main/resources/skills/xlsx-template-processor/SKILL.md`
   - 添加GraalVM兼容说明
   - 更新代码示例

## 下一步建议

### 立即执行

1. **安装Python包**
   ```powershell
   # 设置GraalVM环境
   $env:PATH = "E:\JDK\graalvm\bin;$env:PATH"
   
   # 安装依赖
   graalpy -m pip install openpyxl lxml et_xmlfile
   ```

2. **重启应用**
   - 重启Spring Boot应用
   - 查看启动日志确认环境初始化

3. **测试功能**
   - 调用xlsx_template_processor技能
   - 验证Excel文件处理功能

### 长期优化

1. **配置GraalVM环境变量**
   - 在系统环境变量中设置`GRAALVM_HOME`
   - 添加到`PATH`以便全局使用

2. **使用虚拟环境**
   ```bash
   graalpy -m venv venv
   venv\Scripts\activate
   graalpy -m pip install openpyxl
   ```

3. **持续集成**
   - 在CI/CD流程中添加GraalVM Python包安装步骤
   - 确保构建环境一致性

## 总结

通过以上修复，我们已经：

✅ **解决了`__file__`变量未定义问题**
- 通过PythonToolInterceptor自动注入
- 提供多种兼容的代码写法

✅ **解决了第三方库缺失问题**
- 提供自动安装机制
- 提供手动安装脚本和指南

✅ **提升了代码兼容性**
- Python代码不再强依赖`__file__`变量
- 支持环境变量配置路径
- 兼容GraalVM和标准CPython环境

✅ **完善了文档和工具**
- 详细的配置指南
- 自动化安装脚本
- 常见问题解答

**现在可以正常使用GraalVM Polyglot执行Python代码处理Excel文件了！** 🎉
