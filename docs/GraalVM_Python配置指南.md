# GraalVM Python环境配置指南

## 问题说明

在使用GraalVM Polyglot执行Python代码时，可能会遇到以下三个主要问题：

### 1. `__file__` 变量未定义错误

```
org.graalvm.polyglot.PolyglotException: NameError: name '__file__' is not defined
```

**原因**：GraalVM Python执行环境与标准CPython环境不同，在执行动态生成的Python代码时，`__file__` 变量不会被自动定义。

### 2. 第三方库缺失错误

```
ModuleNotFoundError: No module named 'openpyxl'
```

**原因**：GraalVM Python环境与系统Python环境是隔离的，需要单独安装第三方库。

### 3. 类型注解权限错误

```
org.graalvm.polyglot.PolyglotException: PermissionError: tuple(ObjectSequenceStorage[1, Operation not permitted])
```

**原因**：GraalVM Python 24.1.2 及以下版本对 Python 3.9+ 的类型注解语法（如 `tuple[int, int]`）支持不完整。

**解决方案**：
- 方案一：升级到 GraalVM Python 24.2.1 或更高版本（推荐）
- 方案二：运行修复脚本将 `tuple[int, ...]` 改为 `Tuple[int, ...]`
- 方案三：配置 GraalVM Python 权限

详细解决方案请参考：[GraalVM_Python_权限错误解决方案.md](GraalVM_Python_权限错误解决方案.md)

## 解决方案

### 方案一：自动安装（推荐）

项目已配置自动安装机制，在Spring Boot应用启动时会自动安装所需的Python包。

**配置类**：`GraalVMPythonInitializer.java`

**自动安装的包**：
- openpyxl：Excel文件处理库
- lxml：XML处理库（可选，用于提升Excel处理性能）
- et_xmlfile：openpyxl的依赖库

### 方案二：手动安装

#### Windows系统

1. **设置GraalVM环境变量**

```powershell
# 在PowerShell中设置（临时）
$env:PATH = "$env:GRAALVM_HOME\bin;$env:PATH"

# 或在系统环境变量中永久设置
# GRAALVM_HOME = C:\path\to\graalvm
# Path添加：%GRAALVM_HOME%\bin
```

2. **运行安装脚本**

```powershell
# 在项目根目录执行
.\scripts\install_graalvm_python_packages.bat
```

3. **手动安装命令**

```powershell
# 安装openpyxl
graalpy -m pip install openpyxl

# 安装lxml（可选）
graalpy -m pip install lxml

# 安装et_xmlfile
graalpy -m pip install et_xmlfile
```

#### Linux/macOS系统

```bash
# 设置环境变量
export PATH=$GRAALVM_HOME/bin:$PATH

# 安装包
graalpy -m pip install openpyxl lxml et_xmlfile
```

### 方案三：使用虚拟环境

```bash
# 创建GraalVM Python虚拟环境
graalpy -m venv myenv

# 激活虚拟环境
# Windows:
myenv\Scripts\activate
# Linux/macOS:
source myenv/bin/activate

# 安装包
graalpy -m pip install openpyxl lxml et_xmlfile
```

## 代码适配

### PythonToolInterceptor增强

项目已实现`PythonToolInterceptor`拦截器，自动处理以下问题：

1. **自动注入`__file__`变量**
   - 在Python代码执行前自动定义`__file__`变量
   - 指向临时脚本文件路径

2. **自动配置sys.path**
   - 将skills路径添加到`sys.path`
   - 将工作目录添加到`sys.path`
   - 设置`PYTHONPATH`环境变量

### Python代码最佳实践

为避免环境问题，建议在Python代码中遵循以下最佳实践：

#### ❌ 不推荐的做法

```python
# 使用__file__获取路径
import os
script_dir = os.path.dirname(os.path.abspath(__file__))
sys.path.insert(0, os.path.join(script_dir, 'scripts'))
```

#### ✅ 推荐的做法

```python
# 方式1：使用环境变量或配置
import os
import sys

# 从环境变量获取路径
skills_path = os.environ.get('SKILLS_PATH', '/default/path')
if skills_path not in sys.path:
    sys.path.insert(0, skills_path)

# 方式2：使用绝对路径
import sys
sys.path.insert(0, 'E:/study/AI/taorunhui-ai/src/main/resources/skills/xlsx-template-processor/scripts')

# 方式3：动态检测（已由拦截器处理）
# 拦截器会自动注入环境设置，无需手动处理
```

## 验证安装

### 验证GraalVM Python环境

```powershell
# 检查graalpy版本
graalpy --version

# 检查已安装的包
graalpy -m pip list

# 测试openpyxl导入
graalpy -c "import openpyxl; print('openpyxl版本:', openpyxl.__version__)"
```

### 验证项目集成

运行项目后，查看日志确认：

```
开始初始化GraalVM Python环境...
正在安装Python包: openpyxl
Python包 openpyxl 安装成功
GraalVM Python环境初始化完成
```

## 常见问题

### Q1: 找不到graalpy命令

**解决方法**：
1. 确认GraalVM已正确安装
2. 检查`GRAALVM_HOME`环境变量
3. 将`%GRAALVM_HOME%\bin`添加到`PATH`

### Q2: pip安装失败

**解决方法**：
```powershell
# 升级pip
graalpy -m pip install --upgrade pip

# 使用国内镜像
graalpy -m pip install -i https://pypi.tuna.tsinghua.edu.cn/simple openpyxl
```

### Q3: 导入模块仍然失败

**解决方法**：
1. 检查`sys.path`是否包含正确路径
2. 确认包已安装到GraalVM Python环境而非系统Python
3. 查看拦截器日志确认环境设置已注入

### Q4: Windows路径问题

**解决方法**：
- 使用原始字符串：`r'E:\path\to\file'`
- 或使用正斜杠：`'E:/path/to/file'`
- 或双反斜杠：`'E:\\path\\to\\file'`

## 技术细节

### PythonToolInterceptor工作原理

1. **拦截Python工具调用**
   - 拦截所有名为"python"的工具调用
   - 解析JSON格式的工具参数

2. **注入环境设置**
   ```python
   import sys
   import os
   
   # 定义__file__变量
   if '__file__' not in dir() or not __file__:
       __file__ = r'temp_script.py'
   
   # 配置sys.path
   _skills_path = r'path/to/skills'
   if _skills_path not in sys.path:
       sys.path.insert(0, _skills_path)
   
   # 设置环境变量
   os.environ['PYTHONPATH'] = os.environ.get('PYTHONPATH', '') + os.pathsep + _skills_path
   ```

3. **执行修改后的代码**
   - 将环境设置代码与原始代码合并
   - 传递给Python工具执行

## 参考资料

- [GraalVM Python文档](https://www.graalvm.org/python/)
- [GraalVM Polyglot API](https://www.graalvm.org/sdk/javadoc/org/graalvm/polyglot/package-summary.html)
- [openpyxl文档](https://openpyxl.readthedocs.io/)
