@echo off
REM GraalVM Python包安装脚本
REM 用于在GraalVM Python环境中安装所需的第三方库
REM 
REM 使用方法：
REM 1. 确保GRAALVM_HOME环境变量已设置
REM 2. 以管理员身份运行此脚本

echo ========================================
echo GraalVM Python环境包安装工具
echo ========================================
echo.

REM 检查graalpy命令是否可用
where graalpy >nul 2>nul
if %ERRORLEVEL% neq 0 (
    echo [错误] 未找到graalpy命令
    echo 请确保GraalVM已安装并配置到PATH环境变量中
    echo.
    echo 如果使用IntelliJ IDEA，请在Terminal中执行：
    echo   set PATH=%%GRAALVM_HOME%%\bin;%%PATH%%
    echo   然后重新运行此脚本
    pause
    exit /b 1
)

echo [信息] 找到graalpy命令
graalpy --version
echo.

REM 安装基础包管理工具
echo [步骤1] 升级pip...
graalpy -m pip install --upgrade pip
if %ERRORLEVEL% neq 0 (
    echo [警告] pip升级失败，继续使用当前版本
)
echo.

REM 安装openpyxl及其依赖
echo [步骤2] 安装openpyxl及其依赖...
graalpy -m pip install openpyxl
if %ERRORLEVEL% neq 0 (
    echo [错误] openpyxl安装失败
    pause
    exit /b 1
)
echo.

echo [步骤3] 安装lxml（可选，用于更好的Excel性能）...
graalpy -m pip install lxml
if %ERRORLEVEL% neq 0 (
    echo [警告] lxml安装失败，将使用openpyxl默认引擎
)
echo.

echo [步骤4] 安装et_xmlfile（openpyxl依赖）...
graalpy -m pip install et_xmlfile
if %ERRORLEVEL% neq 0 (
    echo [警告] et_xmlfile安装失败
)
echo.

REM 验证安装
echo [步骤5] 验证安装...
graalpy -c "import openpyxl; print('openpyxl版本:', openpyxl.__version__)"
if %ERRORLEVEL% neq 0 (
    echo [错误] openpyxl导入验证失败
    pause
    exit /b 1
)

echo.
echo ========================================
echo 安装完成！
echo ========================================
echo.
echo 已安装的包列表：
graalpy -m pip list
echo.
pause
