@echo off
chcp 65001 > nul
echo ========================================
echo GraalVM Python 类型注解修复工具
echo ========================================
echo.

REM 检查 Python 是否可用
python --version > nul 2>&1
if %errorlevel% neq 0 (
    echo 错误: 未找到 Python 命令
    echo 请确保 Python 已安装并添加到 PATH 环境变量
    pause
    exit /b 1
)

REM 获取脚本所在目录
set SCRIPT_DIR=%~dp0
set PROJECT_ROOT=%SCRIPT_DIR%..

REM 设置默认的 skills 目录
set SKILLS_DIR=%PROJECT_ROOT%\src\main\resources\skills

REM 如果提供了参数，使用参数作为 skills 目录
if not "%~1"=="" (
    set SKILLS_DIR=%~1
)

echo 目标目录: %SKILLS_DIR%
echo.

REM 检查 skills 目录是否存在
if not exist "%SKILLS_DIR%" (
    echo 错误: 目录不存在: %SKILLS_DIR%
    echo.
    echo 使用方法:
    echo   %~n0 [skills目录路径]
    echo.
    echo 示例:
    echo   %~n0
    echo   %~n0 "E:\study\AI\taorunhui-ai\src\main\resources\skills"
    pause
    exit /b 1
)

echo 开始修复...
echo.

REM 运行 Python 修复脚本
python "%SCRIPT_DIR%fix_graalvm_type_annotations.py" "%SKILLS_DIR%"

if %errorlevel% equ 0 (
    echo.
    echo ========================================
    echo 修复完成！
    echo ========================================
    echo.
    echo 下一步:
    echo 1. 重新启动应用
    echo 2. 测试 Python 技能是否正常工作
) else (
    echo.
    echo ========================================
    echo 修复过程中出现错误
    echo ========================================
)

echo.
pause
