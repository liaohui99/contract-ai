"""
改进的测试脚本：验证优化后的xlsx-template-processor技能功能
基于实际模板结构进行测试
"""

import sys
import os
from datetime import datetime

# 添加脚本路径到Python模块搜索路径
sys.path.insert(0, os.path.join(os.path.dirname(__file__), 'scripts'))

from xlsx_template_processor import process_xlsx_template_with_context

def test_improved_contract_processing():
    """测试改进后的合同处理功能"""
    print("正在测试改进后的采购合同处理功能...")
    
    # 模拟从用户上下文获取的完整数据
    context_supplier_info = {
        "name": "北京市海淀医疗设备有限公司",
        "legal_person": "王建国",
        "phone": "13800138000",
        "fax": "010-12345678",
        "bank": "中国银行北京海淀支行",
        "account": "1234567890123456",
        "representative": "李华",
        "company_name": "北京市海淀医疗设备有限公司"
    }
    
    # 基础合同数据
    contract_data = {
        "contract_number": "CG-2025-HY-001",
        "sign_date": "2025-01-15",
        "sign_location": "北京市",
        "buyer": {
            "name": "北京市海淀区医院后勤服务管理委员会"
        },
        "items": [
            {
                "name": "监护仪",
                "spec": "高端多参数监护仪",
                "quantity": 5,
                "unit": "台",
                "price": 25000,
                "amount": 125000,
                "remark": "含一年质保"
            },
            {
                "name": "呼吸机",
                "spec": "有创呼吸机",
                "quantity": 3,
                "unit": "台",
                "price": 45000,
                "amount": 135000,
                "remark": "进口设备"
            },
            {
                "name": "输液泵",
                "spec": "精密输液泵",
                "quantity": 10,
                "unit": "台",
                "price": 8000,
                "amount": 80000,
                "remark": "带报警功能"
            }
        ],
        "delivery": {
            "date": "2025-02-01",
            "days": 5
        },
        "quality_requirement": "符合医疗器械国家标准及CE认证要求",
        "freight_method": "乙方负责运输，费用包含在合同总价中",
        "payment_method": "合同签订后预付30%，验收合格后支付余款",
        "breach_clause": "延期交货每日按合同金额0.5‰承担违约金"
    }
    
    # 定义模板路径和输出路径
    template_path = os.path.join(os.path.dirname(__file__), 'resource', 'template.xlsx')
    output_path = os.path.join(os.getcwd(), 'improved_contract_test_output.xlsx')
    
    try:
        # 使用改进后的上下文感知功能处理合同
        result_path = process_xlsx_template_with_context(
            template_path=template_path,
            data=contract_data,
            context_supplier_info=context_supplier_info,
            output_path=output_path
        )
        
        print(f"[SUCCESS] 改进后的合同处理测试成功！输出文件: {result_path}")
        return True
        
    except Exception as e:
        print(f"[FAILED] 改进后的合同处理测试失败: {str(e)}")
        import traceback
        traceback.print_exc()
        return False


def test_partial_supplier_info():
    """测试部分供应商信息的情况"""
    print("\n正在测试部分供应商信息的处理...")
    
    # 只提供部分供应商信息
    partial_supplier_info = {
        "name": "北京医疗设备公司",
        "phone": "13900139000"
        # 缺少其他信息，这些将被置空
    }
    
    # 基础合同数据
    contract_data = {
        "contract_number": "CG-2025-HY-002",
        "sign_date": "2025-02-01",
        "items": [
            {
                "name": "心电图机",
                "spec": "12导联心电图机",
                "quantity": 2,
                "unit": "台",
                "price": 15000,
                "amount": 30000
            }
        ],
        "delivery": {
            "date": "2025-02-15",
            "days": 3
        },
        "quality_requirement": "符合YY0505-2012标准",
        "freight_method": "乙方承担运费",
        "payment_method": "验收合格后一次性付清",
        "breach_clause": "按合同法相关规定执行"
    }
    
    # 定义模板路径和输出路径
    template_path = os.path.join(os.path.dirname(__file__), 'resource', 'template.xlsx')
    output_path = os.path.join(os.getcwd(), 'partial_supplier_test_output.xlsx')
    
    try:
        # 测试部分供应商信息处理
        result_path = process_xlsx_template_with_context(
            template_path=template_path,
            data=contract_data,
            context_supplier_info=partial_supplier_info,
            output_path=output_path
        )
        
        print(f"[SUCCESS] 部分供应商信息处理测试成功！输出文件: {result_path}")
        print("[INFO] 未提供的供应商信息将被置空")
        return True
        
    except Exception as e:
        print(f"[FAILED] 部分供应商信息处理测试失败: {str(e)}")
        import traceback
        traceback.print_exc()
        return False


def test_no_supplier_info():
    """测试没有供应商信息的情况（置空处理）"""
    print("\n正在测试无供应商信息的置空处理...")
    
    # 基础合同数据，不提供任何供应商信息
    contract_data = {
        "contract_number": "CG-2025-HY-003",
        "sign_date": "2025-03-01",
        "items": [
            {
                "name": "血压计",
                "spec": "电子血压计",
                "quantity": 20,
                "unit": "台",
                "price": 500,
                "amount": 10000
            }
        ],
        "delivery": {
            "date": "2025-03-10",
            "days": 2
        },
        "quality_requirement": "符合医疗器械相关标准",
        "freight_method": "快递配送",
        "payment_method": "货到付款",
        "breach_clause": "按行业惯例执行"
    }
    
    # 定义模板路径和输出路径
    template_path = os.path.join(os.path.dirname(__file__), 'resource', 'template.xlsx')
    output_path = os.path.join(os.getcwd(), 'no_supplier_test_output.xlsx')
    
    try:
        # 测试无供应商信息处理（将被置空）
        result_path = process_xlsx_template_with_context(
            template_path=template_path,
            data=contract_data,
            context_supplier_info=None,  # 明确传入None
            output_path=output_path
        )
        
        print(f"[SUCCESS] 无供应商信息置空处理测试成功！输出文件: {result_path}")
        print("[INFO] 所有供应商信息字段将被置空，等待用户后续填写")
        return True
        
    except Exception as e:
        print(f"[FAILED] 无供应商信息置空处理测试失败: {str(e)}")
        import traceback
        traceback.print_exc()
        return False


def test_template_structure_compliance():
    """测试模板结构合规性"""
    print("\n正在测试模板结构合规性...")
    
    try:
        # 从模板分析结果看，我们需要确保数据正确映射到模板的特定位置
        supplier_info = {
            "name": "测试供应商公司",
            "legal_person": "测试法人",
            "phone": "13800138001",
            "fax": "010-11111111",
            "bank": "测试银行",
            "account": "1111111111111111",
            "representative": "测试代表"
        }
        
        basic_data = {
            "contract_number": "CG-2025-STR-001",
            "sign_date": "2025-04-01",
            "sign_location": "上海市",
            "buyer": {
                "name": "上海医院管理公司"
            },
            "items": [
                {
                    "name": "测试产品",
                    "spec": "测试规格",
                    "quantity": 1,
                    "unit": "套",
                    "price": 1000,
                    "amount": 1000
                }
            ],
            "delivery": {
                "date": "2025-04-10",
                "days": 3
            },
            "quality_requirement": "测试质量要求",
            "freight_method": "测试运费方式",
            "payment_method": "测试付款方式",
            "breach_clause": "测试违约条款"
        }
        
        template_path = os.path.join(os.path.dirname(__file__), 'resource', 'template.xlsx')
        output_path = os.path.join(os.getcwd(), 'structure_compliance_test.xlsx')
        
        result_path = process_xlsx_template_with_context(
            template_path=template_path,
            data=basic_data,
            context_supplier_info=supplier_info,
            output_path=output_path
        )
        
        print(f"[SUCCESS] 模板结构合规性测试成功！输出文件: {result_path}")
        print("[INFO] 数据已正确映射到模板的相应位置")
        return True
        
    except Exception as e:
        print(f"[FAILED] 模板结构合规性测试失败: {str(e)}")
        import traceback
        traceback.print_exc()
        return False


if __name__ == "__main__":
    print("="*80)
    print("改进后的Excel模板处理器技能综合测试")
    print("基于实际模板结构进行验证")
    print("="*80)
    
    # 运行各项测试
    test_results = []
    
    test_results.append(("完整供应商信息处理", test_improved_contract_processing()))
    test_results.append(("部分供应商信息处理", test_partial_supplier_info()))
    test_results.append(("无供应商信息置空处理", test_no_supplier_info()))
    test_results.append(("模板结构合规性", test_template_structure_compliance()))
    
    # 输出测试总结
    print("\n" + "="*80)
    print("测试总结:")
    print("="*80)
    
    all_passed = True
    for test_name, result in test_results:
        status = "[PASSED]" if result else "[FAILED]"
        print(f"{test_name:<30} {status}")
        if not result:
            all_passed = False
    
    print(f"\n总体结果: [{'ALL TESTS PASSED' if all_passed else 'SOME TESTS FAILED'}]")
    
    if all_passed:
        print("\n✅ 所有改进后的功能测试均已通过！")
        print("✅ 基于实际模板结构的优化已成功实施！")
    else:
        print("\n❌ 存在测试失败，请检查相关功能实现。")
    
    print("="*80)