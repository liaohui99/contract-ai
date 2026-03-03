# 使用示例

以下是一些实际使用xlsx-template-processor技能的示例，展示了如何在不同场景中使用优化后的功能。

## 示例1：基础采购合同生成

```python
from scripts.xlsx_template_processor import process_xlsx_template

# 合同数据（不包含供应商信息，供应商信息将从上下文中获取或置空）
contract_data = {
    "contract_number": "CG-2025-EX-001",
    "sign_date": "2025-01-15",
    "items": [
        {
            "name": "办公设备",
            "spec": "打印机",
            "quantity": 5,
            "unit": "台",
            "price": 1200,
            "remark": "彩色激光打印机"
        },
        {
            "name": "办公耗材", 
            "spec": "墨盒",
            "quantity": 10,
            "unit": "个",
            "price": 200,
            "remark": "黑色墨盒"
        }
    ],
    "delivery": {
        "date": "2025-01-25",
        "days": 3
    },
    "quality_requirement": "符合国家相关标准",
    "freight_method": "乙方承担",
    "payment_method": "验收合格后付款",
    "breach_clause": "按合同法相关规定执行"
}

# 生成合同
result = process_xlsx_template(
    template_path="resource/template.xlsx",
    data=contract_data,
    output_path="contract_output.xlsx"
)
```

## 示例2：使用上下文感知的供应商信息处理

```python
from scripts.xlsx_template_processor import process_xlsx_template_with_context

# 从用户对话上下文获取的供应商信息
supplier_context = {
    "name": "北京科技有限公司",
    "legal_person": "张三",
    "phone": "13800138000",
    "fax": "010-12345678",
    "bank": "中国银行北京分行",
    "account": "1234567890123456",
    "representative": "李四"
}

# 基础合同数据（不含供应商信息）
basic_contract_data = {
    "contract_number": "CG-2025-EX-002",
    "sign_date": "2025-02-01",
    "items": [
        {
            "name": "软件服务",
            "spec": "系统维护",
            "quantity": 12,
            "unit": "个月",
            "price": 5000,
            "remark": "年度维护服务"
        }
    ],
    "delivery": {
        "date": "2025-02-10",
        "days": 5
    },
    "quality_requirement": "服务质量符合行业标准",
    "freight_method": "线上交付",
    "payment_method": "季度付款",
    "breach_clause": "延迟响应超过24小时视为违约"
}

# 使用上下文感知功能生成合同
result = process_xlsx_template_with_context(
    template_path="resource/template.xlsx",
    data=basic_contract_data,
    context_supplier_info=supplier_context,  # 从用户上下文获取的供应商信息
    output_path="context_aware_contract.xlsx"
)
```

## 示例3：处理空供应商上下文

```python
from scripts.xlsx_template_processor import process_xlsx_template_with_context

# 用户未提供供应商信息的情况
supplier_context = None  # 或者 {}

basic_contract_data = {
    "contract_number": "CG-2025-EX-003",
    "sign_date": "2025-03-01",
    "items": [
        {
            "name": "咨询服务",
            "spec": "技术咨询",
            "quantity": 20,
            "unit": "小时",
            "price": 300,
            "remark": "专业顾问服务"
        }
    ],
    "delivery": {
        "date": "2025-03-10",
        "days": 2
    },
    "quality_requirement": "咨询方案需满足客户需求",
    "freight_method": "现场服务",
    "payment_method": "按次结算",
    "breach_clause": "未能按时完成任务需承担责任"
}

# 供应商信息将被置空，等待用户后续填入
result = process_xlsx_template_with_context(
    template_path="resource/template.xlsx",
    data=basic_contract_data,
    context_supplier_info=supplier_context,  # 供应商信息为空
    output_path="empty_supplier_contract.xlsx"
)
```

## 示例4：处理部分供应商信息

```python
from scripts.xlsx_template_processor import process_xlsx_template_with_context

# 用户仅提供了部分供应商信息
partial_supplier_context = {
    "name": "上海技术服务公司",
    "phone": "13900139000"
    # 缺少其他供应商信息，这些字段将被置空
}

basic_contract_data = {
    "contract_number": "CG-2025-EX-004",
    "sign_date": "2025-04-01",
    "items": [
        {
            "name": "硬件设备",
            "spec": "服务器",
            "quantity": 3,
            "unit": "台",
            "price": 25000,
            "remark": "高性能服务器"
        }
    ],
    "delivery": {
        "date": "2025-04-15",
        "days": 7
    },
    "quality_requirement": "设备需通过性能测试",
    "freight_method": "快递配送",
    "payment_method": "预付50%，验收后付清",
    "breach_clause": "设备故障率超过1%需更换"
}

# 部分供应商信息会被使用，缺失信息将被置空
result = process_xlsx_template_with_context(
    template_path="resource/template.xlsx",
    data=basic_contract_data,
    context_supplier_info=partial_supplier_context,  # 部分供应商信息
    output_path="partial_supplier_contract.xlsx"
)
```

## 实际应用场景

### 场景1：智能合同生成系统
在智能合同生成系统中，用户可能会分步提供信息：
1. 首先提供合同基本信息（编号、日期、商品等）
2. 然后提供供应商信息
3. 最后确认所有信息并生成合同

使用 `process_xlsx_template_with_context` 可以很好地处理这种分步场景。

### 场景2：对话式合同助手
在对话式AI助手中，供应商信息可能来自之前的对话历史：
1. AI从对话历史中提取供应商信息
2. 将提取的信息作为上下文传递给模板处理器
3. 如果没有找到相关信息，则将供应商字段置空

### 场景3：批量合同处理
当处理大量合同但供应商信息来源不一致时：
1. 对于已知供应商，使用完整信息填充
2. 对于未知供应商，生成空白模板供后续填写
3. 保持处理流程的一致性

这些示例展示了优化后的xlsx-template-processor技能如何更好地适应实际业务需求，特别是在处理供应商信息时的灵活性。