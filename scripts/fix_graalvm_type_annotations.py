import re
from pathlib import Path

def fix_type_annotations(file_path):
    """
    修复文件中的类型注解，将 Python 3.9+ 的语法改为兼容语法
    
    Args:
        file_path: Python 文件路径
        
    Returns:
        bool: 如果文件被修改返回 True，否则返回 False
    """
    try:
        with open(file_path, 'r', encoding='utf-8') as f:
            content = f.read()
        
        original_content = content
        
        # 替换 tuple[int, ...] 为 Tuple[int, ...]
        # 使用正则表达式匹配所有 tuple[...] 模式
        content = re.sub(r'\btuple\[', r'Tuple[', content)
        
        # 检查是否需要添加 typing 导入
        if 'Tuple[' in content and 'from typing import' not in content:
            # 在文件开头添加导入
            lines = content.split('\n')
            import_line = -1
            
            # 找到第一个 import 语句的位置
            for i, line in enumerate(lines):
                stripped = line.strip()
                if stripped.startswith('import ') or stripped.startswith('from '):
                    import_line = i
                    break
            
            # 添加 typing 导入
            typing_import = 'from typing import Tuple'
            
            if import_line >= 0:
                # 在第一个 import 语句之前插入
                lines.insert(import_line, typing_import)
            else:
                # 如果没有 import 语句，在文件开头插入
                lines.insert(0, typing_import)
            
            content = '\n'.join(lines)
        
        # 如果内容有变化，写回文件
        if content != original_content:
            with open(file_path, 'w', encoding='utf-8') as f:
                f.write(content)
            return True
            
        return False
        
    except Exception as e:
        print(f"处理文件 {file_path} 时出错: {e}")
        return False

def fix_all_files(skills_dir):
    """
    修复指定目录下所有 Python 文件的类型注解
    
    Args:
        skills_dir: skills 目录路径
    """
    skills_path = Path(skills_dir)
    
    if not skills_path.exists():
        print(f"错误: 目录 {skills_dir} 不存在")
        return
    
    print(f"开始扫描目录: {skills_dir}")
    print("=" * 60)
    
    fixed_count = 0
    scanned_count = 0
    
    # 递归查找所有 .py 文件
    for py_file in skills_path.rglob('*.py'):
        scanned_count += 1
        if fix_type_annotations(py_file):
            fixed_count += 1
            print(f"✓ 已修复: {py_file.relative_to(skills_path)}")
    
    print("=" * 60)
    print(f"扫描完成: 共扫描 {scanned_count} 个 Python 文件")
    print(f"修复完成: 共修复 {fixed_count} 个文件")
    
    if fixed_count > 0:
        print("\n提示: 请重新启动应用以应用更改")

def main():
    """主函数"""
    # 默认 skills 目录
    skills_directory = 'src/main/resources/skills'
    
    # 也可以从命令行参数获取目录
    import sys
    if len(sys.argv) > 1:
        skills_directory = sys.argv[1]
    
    print("GraalVM Python 类型注解修复工具")
    print("=" * 60)
    print(f"目标目录: {skills_directory}")
    print()
    
    fix_all_files(skills_directory)

if __name__ == '__main__':
    main()
