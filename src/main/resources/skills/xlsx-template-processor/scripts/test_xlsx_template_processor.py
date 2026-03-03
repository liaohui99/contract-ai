"""
测试脚本：验证xlsx-template-processor技能的功能
"""

import sys
import os
from datetime import datetime

# 添加脚本路径到Python模块搜索路径
sys.path.insert(0, os.path.join(os.path.dirname(__file__), '..','scripts'))

from xlsx_template_processor import process_xlsx_template, fill_template_from_dict

def test_basic_functionality():
    """测试基本功能"""
    print("正在测试Excel模板处理器的基本功能...")
    
    # 准备测试数据 - 针对采购合同模板
    test_data = {
        "contract_number": "CG-2025-TEST-001",
        "sign_date": datetime.now(),
        "supplier": {
            "name": "测试供应商有限公司",
            "legal_person": "测试法人",
            "phone": "13800138000",
            "fax": "010-12345678",
            "bank": "测试银行支行",
            "account": "1234567890123456789",
            "representative": "测试代表",
            "company_name": "测试供应商有限公司"
        },
        "items": [
            {
                "name": "测试商品1",
                "spec": "规格型号A",
                "quantity": 10,
                "unit": "件",
                "price": 100,
                "remark": "无"
            },
            {
                "name": "测试商品2", 
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
    
    # 定义模板路径和输出路径
    template_path = os.path.join(os.path.dirname(__file__), '..', 'resource', 'template.xlsx')
    output_path = os.path.join(os.getcwd(), 'test_output.xlsx')
    
    try:
        # 测试process_xlsx_template函数
        result_path = process_xlsx_template(
            template_path=template_path,
            data=test_data,
            output_path=output_path
        )
        
        print(f"[SUCCESS] 模板处理成功！输出文件: {result_path}")
        return True
        
    except Exception as e:
        print(f"[FAILED] 模板处理失败: {str(e)}")
        return False

def test_advanced_functionality():
    """测试高级功能"""
    print("\n正在测试Excel模板处理器的高级功能...")
    
    # 准备更复杂的测试数据
    advanced_data = {
        "contract_number": "CG-2025-ADV-001",
        "sign_date": "2025-01-15",
        "supplier": {
            "name": "高级测试供应商",
            "legal_person": "高级测试法人",
            "phone": "13900139000",
            "fax": "010-98765432",
            "bank": "高级测试银行",
            "account": "9876543210987654321",
            "representative": "高级测试代表",
            "company_name": "高级测试供应商"
        },
        "items": [
            {
                "name": "高级商品1",
                "spec": "高级规格A",
                "quantity": 100,
                "unit": "套",
                "price": 500,
                "amount": 50000,
                "remark": "批量优惠"
            },
            {
                "name": "高级商品2",
                "spec": "高级规格B", 
                "quantity": 50,
                "unit": "台",
                "price": 1000,
                "amount": 50000,
                "remark": "定制产品"
            },
            {
                "name": "高级商品3",
                "spec": "高级规格C",
                "quantity": 10,
                "unit": "件",
                "price": 2000,
                "amount": 20000,
                "remark": "高价值产品"
            }
        ],
        "delivery": {
            "date": "2025-02-01",
            "days": 5
        },
        "quality_requirement": "符合ISO 9001质量管理体系标准",
        "freight_method": "甲方指定物流商，费用由乙方承担",
        "payment_method": "合同签订后预付30%，发货后付清余款",
        "breach_clause": "延期交货每日按合同总额0.5‰承担违约金"
    }
    
    template_path = os.path.join(os.path.dirname(__file__), '..', 'resource', 'template.xlsx')
    output_path = os.path.join(os.getcwd(), 'advanced_test_output.xlsx')
    
    try:
        # 测试fill_template_from_dict函数
        result_path = fill_template_from_dict(
            template_path=template_path,
            data=advanced_data,
            output_path=output_path
        )
        
        print(f"[SUCCESS] 高级功能测试成功！输出文件: {result_path}")
        return True
        
    except Exception as e:
        print(f"[FAILED] 高级功能测试失败: {str(e)}")
        import traceback
        traceback.print_exc()
        return False

def test_user_input_integration():
    """测试用户输入集成功能"""
    print("\n正在测试用户输入集成功能...")
    
    # 模拟用户对话数据 - 更贴近真实使用场景
    user_dialogue_data = {
        "contract_number": "CG-2025-USER-001",
        "sign_date": "2025-03-15",
        "supplier": {
            "name": "用户指定供应商",
            "legal_person": "用户法定代表人",
            "phone": "13700137000",
            "fax": "021-12345678",
            "bank": "用户指定银行",
            "account": "用户银行账号",
            "representative": "用户代表",
            "company_name": "用户指定供应商"
        },
        "items": [
            {
                "name": "用户商品1",
                "spec": "用户规格A",
                "quantity": 50,
                "unit": "个",
                "price": 200,
                "remark": "用户备注"
            },
            {
                "name": "用户商品2",
                "spec": "用户规格B",
                "quantity": 25,
                "unit": "台",
                "price": 800,
                "remark": "重要商品"
            }
        ],
        "delivery": {
            "date": "2025-03-25",
            "days": 5
        },
        "quality_requirement": "满足用户特定需求",
        "freight_method": "按用户要求配送",
        "payment_method": "用户指定付款方式",
        "breach_clause": "按用户约定承担违约责任"
    }
    
    template_path = os.path.join(os.path.dirname(__file__), '..', 'resource', 'template.xlsx')
    output_path = os.path.join(os.getcwd(), 'user_input_test_output.xlsx')
    
    try:
        result_path = process_xlsx_template(
            template_path=template_path,
            data=user_dialogue_data,
            output_path=output_path
        )
        
        print(f"[SUCCESS] 用户输入集成测试成功！输出文件: {result_path}")
        return True
        
    except Exception as e:
        print(f"[FAILED] 用户输入集成测试失败: {str(e)}")
        import traceback
        traceback.print_exc()
        return False

if __name__ == "__main__":
    print("="*60)
    print("Excel模板处理器技能测试")
    print("="*60)
    
    # 运行基本功能测试
    basic_success = test_basic_functionality()
    
    # 运行高级功能测试
    advanced_success = test_advanced_functionality()
    
    # 运行用户输入集成测试
    user_input_success = test_user_input_integration()
    
    print("\n" + "="*60)
    print("测试总结:")
    print(f"基本功能测试: {'[PASSED]' if basic_success else '[FAILED]'}")
    print(f"高级功能测试: {'[PASSED]' if advanced_success else '[FAILED]'}")
    print(f"用户输入集成测试: {'[PASSED]' if user_input_success else '[FAILED]'}")
    
    all_tests_passed = basic_success and advanced_success and user_input_success
    if all_tests_passed:
        print("\n[ALL TESTS PASSED] 所有测试均通过！Excel模板处理器技能运行正常。")
    else:
        print("\n[ISSUES FOUND] 存在测试失败，请检查代码实现。")
    
    print("="*60)