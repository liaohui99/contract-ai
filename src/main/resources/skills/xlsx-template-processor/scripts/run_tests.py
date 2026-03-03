"""
整合测试脚本：运行所有xlsx-template-processor技能的测试
此脚本用于执行所有相关的测试，验证技能功能的完整性
"""

import subprocess
import sys
import os

def run_test_script(script_name):
    """运行指定的测试脚本"""
    script_path = os.path.join(os.path.dirname(__file__), 'scripts', script_name)
    
    if not os.path.exists(script_path):
        print(f"[WARNING] 测试脚本不存在: {script_path}")
        return False
    
    print(f"\n{'='*60}")
    print(f"正在运行测试: {script_name}")
    print(f"{'='*60}")
    
    try:
        result = subprocess.run([sys.executable, script_path], 
                              capture_output=True, text=True, timeout=120)
        
        print(result.stdout)
        if result.stderr:
            print("错误输出:")
            print(result.stderr)
        
        success = result.returncode == 0
        print(f"\n[{'SUCCESS' if success else 'FAILED'}] {script_name} - 返回码: {result.returncode}")
        return success
        
    except subprocess.TimeoutExpired:
        print(f"[TIMEOUT] {script_name} - 测试超时")
        return False
    except Exception as e:
        print(f"[ERROR] 运行 {script_name} 时发生异常: {str(e)}")
        return False

def main():
    """主测试函数"""
    print("Excel模板处理器技能综合测试")
    print("="*70)
    
    # 定义要运行的测试脚本
    test_scripts = [
        'test_xlsx_template_processor.py',           # 基础功能测试
        'test_context_aware_xlsx_processor.py',      # 上下文感知功能测试
        '../improved_test.py'                        # 基于实际模板结构的改进测试
    ]
    
    results = {}
    
    # 运行所有测试脚本
    for script in test_scripts:
        results[script] = run_test_script(script)
    
    # 输出测试总结
    print(f"\n{'='*70}")
    print("综合测试结果:")
    print(f"{'='*70}")
    
    all_passed = True
    for script, result in results.items():
        status = "[PASSED]" if result else "[FAILED]"
        print(f"{script:<40} {status}")
        if not result:
            all_passed = False
    
    print(f"\n总体结果: [{'ALL TESTS PASSED' if all_passed else 'SOME TESTS FAILED'}]")
    
    if all_passed:
        print("\nAll tests passed! xlsx-template-processor skill functions properly.")
    else:
        print("\nSome tests failed, please check the implementation.")
    
    print("="*70)
    
    return all_passed

if __name__ == "__main__":
    success = main()
    sys.exit(0 if success else 1)