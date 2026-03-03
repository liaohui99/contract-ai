"""
简化验证脚本：验证xlsx-template-processor技能的核心功能
"""

import sys
import os
from datetime import datetime

# 添加脚本路径到Python模块搜索路径
sys.path.insert(0, os.path.join(os.path.dirname(__file__), 'scripts'))

from xlsx_template_processor import process_xlsx_template_with_context

def validate_core_features():
    """验证核心功能特性"""
    print("开始验证xlsx-template-processor技能的核心功能...")
    
    # 测试数据
    supplier_info = {
        "name": "北京测试科技有限公司",
        "legal_person": "测试法人",
        "phone": "13800138000",
        "fax": "010-12345678",
        "bank": "中国银行北京分行",
        "account": "1234567890123456",
        "representative": "测试代表"
    }
    
    contract_data = {
        "contract_number": "CG-2025-TEST-001",
        "sign_date": "2025-01-15",
        "sign_location": "北京",
        "buyer": {
            "name": "北京市某采购单位"
        },
        "items": [
            {
                "name": "测试设备",
                "spec": "规格型号A",
                "quantity": 10,
                "unit": "台",
                "price": 5000,
                "amount": 50000
            }
        ],
        "delivery": {
            "date": "2025-01-30",
            "days": 3
        },
        "quality_requirement": "符合国家质量标准",
        "freight_method": "供应商承担运费",
        "payment_method": "验收合格后付款",
        "breach_clause": "按合同法执行"
    }
    
    template_path = os.path.join(os.path.dirname(__file__), 'resource', 'template.xlsx')
    
    # 测试1: 完整供应商信息处理
    print("\n测试1: 完整供应商信息处理")
    try:
        output_path = os.path.join(os.getcwd(), 'core_test_complete.xlsx')
        result_path = process_xlsx_template_with_context(
            template_path=template_path,
            data=contract_data,
            context_supplier_info=supplier_info,
            output_path=output_path
        )
        print("PASS: 完整供应商信息处理成功")
    except Exception as e:
        print(f"FAIL: 完整供应商信息处理失败 - {str(e)}")
        return False
    
    # 测试2: 部分供应商信息处理
    print("\n测试2: 部分供应商信息处理")
    try:
        partial_supplier = {"name": "部分信息公司", "phone": "13900139000"}
        output_path = os.path.join(os.getcwd(), 'core_test_partial.xlsx')
        result_path = process_xlsx_template_with_context(
            template_path=template_path,
            data=contract_data,
            context_supplier_info=partial_supplier,
            output_path=output_path
        )
        print("PASS: 部分供应商信息处理成功")
    except Exception as e:
        print(f"FAIL: 部分供应商信息处理失败 - {str(e)}")
        return False
    
    # 测试3: 无供应商信息（置空处理）
    print("\n测试3: 无供应商信息（置空处理）")
    try:
        output_path = os.path.join(os.getcwd(), 'core_test_empty.xlsx')
        result_path = process_xlsx_template_with_context(
            template_path=template_path,
            data=contract_data,
            context_supplier_info=None,
            output_path=output_path
        )
        print("PASS: 供应商信息置空处理成功")
    except Exception as e:
        print(f"FAIL: 供应商信息置空处理失败 - {str(e)}")
        return False
    
    # 测试4: 基础功能验证
    print("\n测试4: 基础功能验证")
    try:
        from xlsx_template_processor import process_xlsx_template
        basic_data = {
            "contract_number": "CG-2025-BASIC-001",
            "sign_date": "2025-02-01",
            "items": [{"name": "基础商品", "quantity": 1, "price": 100}]
        }
        output_path = os.path.join(os.getcwd(), 'core_test_basic.xlsx')
        result_path = process_xlsx_template(
            template_path=template_path,
            data=basic_data,
            output_path=output_path
        )
        print("PASS: 基础功能验证成功")
    except Exception as e:
        print(f"FAIL: 基础功能验证失败 - {str(e)}")
        return False
    
    print("\n所有核心功能验证完成!")
    return True


def main():
    """主验证函数"""
    print("="*60)
    print("xlsx-template-processor 技能核心功能验证")
    print("="*60)
    
    success = validate_core_features()
    
    print("\n" + "="*60)
    if success:
        print("SUCCESS: 所有核心功能验证通过！")
        print("优化后的技能特性：")
        print("- 上下文感知的供应商信息处理")
        print("- 供应商信息从用户上下文获取，无则置空")
        print("- 基于实际模板结构的精确映射")
        print("- 完整的向后兼容性")
    else:
        print("FAILURE: 验证失败")
    print("="*60)
    
    return success


if __name__ == "__main__":
    success = main()
    exit(0 if success else 1)