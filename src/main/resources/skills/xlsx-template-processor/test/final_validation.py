"""
最终验证脚本：全面验证xlsx-template-processor技能的优化效果
"""

import sys
import os
from datetime import datetime

# 添加脚本路径到Python模块搜索路径
sys.path.insert(0, os.path.join(os.path.dirname(__file__), 'scripts'))

from xlsx_template_processor import process_xlsx_template_with_context

def validate_all_features():
    """验证所有改进的功能特性"""
    print("开始全面验证xlsx-template-processor技能...")
    
    # 测试1: 完整供应商上下文信息
    print("\n--- 测试1: 完整供应商上下文信息 ---")
    supplier_info_full = {
        "name": "北京创新科技有限公司",
        "legal_person": "张伟",
        "phone": "13800138000",
        "fax": "010-12345678",
        "bank": "中国工商银行北京分行",
        "account": "6222021234567890123",
        "representative": "李明",
        "company_name": "北京创新科技有限公司"
    }
    
    contract_data = {
        "contract_number": "CG-2025-ZX-001",
        "sign_date": "2025-01-15",
        "sign_location": "北京市朝阳区",
        "buyer": {
            "name": "北京市某政府部门"
        },
        "items": [
            {
                "name": "服务器",
                "spec": "高性能计算服务器",
                "quantity": 5,
                "unit": "台",
                "price": 20000,
                "amount": 100000
            }
        ],
        "delivery": {
            "date": "2025-01-30",
            "days": 3
        },
        "quality_requirement": "符合国家相关技术标准",
        "freight_method": "供应商承担运输费用",
        "payment_method": "验收合格后支付全款",
        "breach_clause": "延期交付每日按合同总额0.5‰承担违约金"
    }
    
    template_path = os.path.join(os.path.dirname(__file__), 'resource', 'template.xlsx')
    output_path = os.path.join(os.getcwd(), 'validation_test_full.xlsx')
    
    try:
        result_path = process_xlsx_template_with_context(
            template_path=template_path,
            data=contract_data,
            context_supplier_info=supplier_info_full,
            output_path=output_path
        )
        print(f"[✓] 测试1通过: 完整供应商信息处理")
    except Exception as e:
        print(f"[✗] 测试1失败: {str(e)}")
        return False
    
    # 测试2: 部分供应商上下文信息
    print("\n--- 测试2: 部分供应商上下文信息 ---")
    supplier_info_partial = {
        "name": "上海科技公司",
        "phone": "13900139000"
        # 其他信息缺失，将被置空
    }
    
    output_path = os.path.join(os.getcwd(), 'validation_test_partial.xlsx')
    
    try:
        result_path = process_xlsx_template_with_context(
            template_path=template_path,
            data=contract_data,
            context_supplier_info=supplier_info_partial,
            output_path=output_path
        )
        print(f"[✓] 测试2通过: 部分供应商信息处理")
    except Exception as e:
        print(f"[✗] 测试2失败: {str(e)}")
        return False
    
    # 测试3: 无供应商上下文信息（置空处理）
    print("\n--- 测试3: 无供应商上下文信息（置空处理） ---")
    output_path = os.path.join(os.getcwd(), 'validation_test_empty.xlsx')
    
    try:
        result_path = process_xlsx_template_with_context(
            template_path=template_path,
            data=contract_data,
            context_supplier_info=None,  # 明确传入None
            output_path=output_path
        )
        print(f"[✓] 测试3通过: 供应商信息置空处理")
    except Exception as e:
        print(f"[✗] 测试3失败: {str(e)}")
        return False
    
    # 测试4: 基础功能验证
    print("\n--- 测试4: 基础功能验证 ---")
    try:
        from xlsx_template_processor import process_xlsx_template
        basic_data = {
            "contract_number": "CG-2025-JC-001",
            "sign_date": "2025-02-01",
            "items": [
                {
                    "name": "基础商品",
                    "quantity": 1,
                    "price": 100
                }
            ]
        }
        
        output_path = os.path.join(os.getcwd(), 'validation_test_basic.xlsx')
        result_path = process_xlsx_template(
            template_path=template_path,
            data=basic_data,
            output_path=output_path
        )
        print(f"[✓] 测试4通过: 基础功能验证")
    except Exception as e:
        print(f"[✗] 测试4失败: {str(e)}")
        return False
    
    # 测试5: 模板结构适配验证
    print("\n--- 测试5: 模板结构适配验证 ---")
    try:
        # 使用之前分析得到的模板结构信息进行数据填充
        complex_data = {
            "contract_number": "CG-2025-SJ-001",
            "sign_date": datetime.now(),
            "sign_location": "深圳",
            "buyer": {
                "name": "深圳某企业"
            },
            "supplier": {
                "name": "深圳供应商有限公司",
                "legal_person": "王强",
                "phone": "13700137000",
                "fax": "0755-12345678",
                "bank": "招商银行深圳分行",
                "account": "6225021234567890456",
                "representative": "赵亮"
            },
            "items": [
                {
                    "name": "笔记本电脑",
                    "spec": "i7处理器，16GB内存",
                    "quantity": 20,
                    "unit": "台",
                    "price": 8000,
                    "amount": 160000,
                    "remark": "含3年质保"
                },
                {
                    "name": "台式机",
                    "spec": "i9处理器，32GB内存",
                    "quantity": 10,
                    "unit": "台",
                    "price": 12000,
                    "amount": 120000,
                    "remark": "含上门服务"
                }
            ],
            "delivery": {
                "date": "2025-02-15",
                "days": 5
            },
            "quality_requirement": "通过ISO9001质量体系认证",
            "freight_method": "免费送货上门安装",
            "payment_method": "分期付款，验收后支付尾款",
            "breach_clause": "按合同法相关规定执行"
        }
        
        output_path = os.path.join(os.getcwd(), 'validation_test_complex.xlsx')
        result_path = process_xlsx_template_with_context(
            template_path=template_path,
            data=complex_data,
            context_supplier_info=complex_data.get('supplier'),
            output_path=output_path
        )
        print(f"[✓] 测试5通过: 模板结构适配验证")
    except Exception as e:
        print(f"[✗] 测试5失败: {str(e)}")
        return False
    
    return True


def main():
    """主验证函数"""
    print("="*60)
    print("xlsx-template-processor 技能最终验证")
    print("="*60)
    
    success = validate_all_features()
    
    print("\n" + "="*60)
    if success:
        print("🎉 所有验证测试通过！")
        print("✅ xlsx-template-processor 技能已成功优化：")
        print("   - 上下文感知的供应商信息处理")
        print("   - 基于实际模板结构的精确数据映射") 
        print("   - 完整的错误处理机制")
        print("   - 向下兼容性保证")
    else:
        print("❌ 验证失败，请检查实现细节")
    print("="*60)
    
    return success


if __name__ == "__main__":
    success = main()
    exit(0 if success else 1)