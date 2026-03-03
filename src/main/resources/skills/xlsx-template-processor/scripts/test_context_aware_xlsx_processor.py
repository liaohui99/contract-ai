"""
测试脚本：验证xlsx-template-processor技能的上下文感知供应商信息功能
"""

import sys
import os
from datetime import datetime

# 添加脚本路径到Python模块搜索路径
sys.path.insert(0, os.path.join(os.path.dirname(__file__), '..','scripts'))

from xlsx_template_processor import process_xlsx_template_with_context

def test_context_aware_supplier_info():
    """测试上下文感知的供应商信息功能"""
    print("正在测试上下文感知的供应商信息功能...")
    
    # 基础合同数据（不包含供应商信息）
    basic_data = {
        "contract_number": "CG-2025-CONTEXT-001",
        "sign_date": datetime.now(),
        "items": [
            {
                "name": "上下文测试商品1",
                "spec": "规格型号A",
                "quantity": 10,
                "unit": "件",
                "price": 100,
                "remark": "无"
            },
            {
                "name": "上下文测试商品2", 
                "spec": "规格型号B",
                "quantity": 20,
                "unit": "台",
                "price": 200,
                "remark": "特殊处理"
            }
        ],
        "delivery": {
            "date": datetime.now().strftime('%Y-%m-%d'),
            "days": 3
        },
        "quality_requirement": "符合相关标准",
        "freight_method": "乙方负责",
        "payment_method": "验收合格后付款",
        "breach_clause": "按合同法规定执行"
    }
    
    # 从用户上下文获取的供应商信息
    context_supplier_info = {
        "name": "上下文感知供应商有限公司",
        "legal_person": "上下文法定代表人",
        "phone": "13800138000",
        "fax": "010-12345678",
        "bank": "上下文银行支行",
        "account": "1234567890123456789",
        "representative": "上下文代表"
    }
    
    # 定义模板路径和输出路径
    template_path = os.path.join(os.path.dirname(__file__), '..', 'resource', 'template.xlsx')
    output_path = os.path.join(os.getcwd(), 'context_aware_test_output.xlsx')
    
    try:
        # 测试process_xlsx_template_with_context函数
        result_path = process_xlsx_template_with_context(
            template_path=template_path,
            data=basic_data,
            context_supplier_info=context_supplier_info,
            output_path=output_path
        )
        
        print(f"[SUCCESS] 上下文感知功能测试成功！输出文件: {result_path}")
        return True
        
    except Exception as e:
        print(f"[FAILED] 上下文感知功能测试失败: {str(e)}")
        import traceback
        traceback.print_exc()
        return False

def test_empty_supplier_context():
    """测试空供应商上下文的情况"""
    print("\n正在测试空供应商上下文的情况...")
    
    # 基础合同数据（不包含供应商信息）
    basic_data = {
        "contract_number": "CG-2025-EMPTY-001",
        "sign_date": "2025-01-15",
        "items": [
            {
                "name": "空上下文测试商品",
                "spec": "规格型号A",
                "quantity": 5,
                "unit": "件",
                "price": 50,
                "remark": "测试空上下文"
            }
        ],
        "delivery": {
            "date": "2025-01-20",
            "days": 2
        },
        "quality_requirement": "常规质量要求",
        "freight_method": "甲方承担",
        "payment_method": "预付款30%",
        "breach_clause": "按行业标准"
    }
    
    # 空的供应商上下文信息（模拟没有从用户上下文获取到供应商信息的情况）
    context_supplier_info = None  # 或者传入空字典 {}
    
    # 定义模板路径和输出路径
    template_path = os.path.join(os.path.dirname(__file__), '..', 'resource', 'template.xlsx')
    output_path = os.path.join(os.getcwd(), 'empty_context_test_output.xlsx')
    
    try:
        # 测试process_xlsx_template_with_context函数，使用空上下文
        result_path = process_xlsx_template_with_context(
            template_path=template_path,
            data=basic_data,
            context_supplier_info=context_supplier_info,
            output_path=output_path
        )
        
        print(f"[SUCCESS] 空上下文测试成功！输出文件: {result_path}")
        print("[INFO] 在这种情况下，供应商信息字段将被设置为空白")
        return True
        
    except Exception as e:
        print(f"[FAILED] 空上下文测试失败: {str(e)}")
        import traceback
        traceback.print_exc()
        return False

def test_partial_supplier_context():
    """测试部分供应商上下文的情况"""
    print("\n正在测试部分供应商上下文的情况...")
    
    # 基础合同数据
    basic_data = {
        "contract_number": "CG-2025-PARTIAL-001",
        "sign_date": "2025-02-15",
        "items": [
            {
                "name": "部分上下文测试商品",
                "spec": "规格型号B",
                "quantity": 15,
                "unit": "套",
                "price": 200,
                "remark": "测试部分上下文"
            }
        ],
        "delivery": {
            "date": "2025-02-25",
            "days": 5
        },
        "quality_requirement": "高质量标准",
        "freight_method": "乙方承担",
        "payment_method": "货到付款",
        "breach_clause": "严格按约执行"
    }
    
    # 部分供应商上下文信息（有些字段缺失，模拟用户只提供了部分信息）
    context_supplier_info = {
        "name": "部分信息供应商",
        "phone": "13900139000",
        # 缺少 legal_person, fax, bank, account, representative 字段
    }
    
    # 定义模板路径和输出路径
    template_path = os.path.join(os.path.dirname(__file__), '..', 'resource', 'template.xlsx')
    output_path = os.path.join(os.getcwd(), 'partial_context_test_output.xlsx')
    
    try:
        # 测试process_xlsx_template_with_context函数，使用部分上下文
        result_path = process_xlsx_template_with_context(
            template_path=template_path,
            data=basic_data,
            context_supplier_info=context_supplier_info,
            output_path=output_path
        )
        
        print(f"[SUCCESS] 部分上下文测试成功！输出文件: {result_path}")
        print("[INFO] 在这种情况下，提供的供应商信息会被使用，缺失的信息将被设置为空白")
        return True
        
    except Exception as e:
        print(f"[FAILED] 部分上下文测试失败: {str(e)}")
        import traceback
        traceback.print_exc()
        return False

if __name__ == "__main__":
    print("="*70)
    print("Excel模板处理器技能 - 上下文感知供应商信息功能测试")
    print("="*70)
    
    # 运行上下文感知功能测试
    context_success = test_context_aware_supplier_info()
    
    # 运行空上下文测试
    empty_context_success = test_empty_supplier_context()
    
    # 运行部分上下文测试
    partial_context_success = test_partial_supplier_context()
    
    print("\n" + "="*70)
    print("测试总结:")
    print(f"上下文感知功能测试: {'[PASSED]' if context_success else '[FAILED]'}")
    print(f"空上下文测试: {'[PASSED]' if empty_context_success else '[FAILED]'}")
    print(f"部分上下文测试: {'[PASSED]' if partial_context_success else '[FAILED]'}")
    
    all_tests_passed = context_success and empty_context_success and partial_context_success
    if all_tests_passed:
        print("\n[ALL TESTS PASSED] 所有上下文感知功能测试均通过！")
    else:
        print("\n[ISSUES FOUND] 存在测试失败，请检查代码实现。")
    
    print("="*70)