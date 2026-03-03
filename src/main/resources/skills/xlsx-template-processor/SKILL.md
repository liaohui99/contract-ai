---
name: "xlsx_template_processor"
description: "Processes Excel template files (.xlsx) to generate new Excel files based on input data. Use this skill when user needs to fill an Excel template with specific data values or transform data from one Excel format to another using a template. Optimized for processing procurement contracts and similar business documents. Invoke when user provides an Excel template file and data to populate it."
---

# Excel模板处理器技能

本技能用于处理Excel模板文件，根据输入的数据生成新的Excel文件。特别针对采购合同等业务文档进行了优化。

## 功能特点

 1. **智能模板检测**：自动识别采购合同等特定模板格式
 2. **专用处理逻辑**：针对业务文档优化的处理流程
 3. **安全单元格操作**：避免合并单元格导致的错误
 4. **动态表格扩展**：根据数据量自动扩展表格行数
 5. **金额中文转换**：自动将数字金额转换为中文大写
 6. **格式保持**：保留模板中的原有格式和样式
 7. **日期处理**：自动格式化日期类型的值
 8. **嵌套数据支持**：支持处理嵌套的字典和列表数据
 9. **上下文感知的供应商信息处理**：乙方（供方）信息从用户上下文对话中获取，未提供则默认置空
 10. **实际模板结构适配**：基于对实际Excel模板的分析，精确映射数据到模板的正确位置

## 使用方法

### 安装依赖
```bash
# 标准Python环境
pip install openpyxl

# GraalVM Python环境
graalpy -m pip install openpyxl
```

### 方式一：使用 process_xlsx_template 函数（GraalVM兼容）

```python
import sys
import os

# GraalVM兼容的路径设置方式
# 方式1：使用环境变量（推荐，由PythonToolInterceptor自动设置）
skills_path = os.environ.get('SKILLS_PATH', '')
if skills_path and skills_path not in sys.path:
    sys.path.insert(0, skills_path)

# 方式2：使用绝对路径
# sys.path.insert(0, 'E:/study/AI/taorunhui-ai/src/main/resources/skills/xlsx-template-processor/scripts')

# 方式3：动态检测__file__（由拦截器注入，安全使用）
# if '__file__' in dir():
#     script_dir = os.path.dirname(os.path.abspath(__file__))
#     sys.path.insert(0, os.path.join(script_dir, 'scripts'))

from xlsx_template_processor import process_xlsx_template

# 处理Excel模板
result_path = process_xlsx_template(
    template_path="template.xlsx",
    data={
        "contract_number": "CG-2025-001",
        "sign_date": "2025-01-15",
        "supplier": {
            "name": "供应商名称",
            "legal_person": "法定代表人",
            "phone": "联系电话",
            "fax": "传真",
            "bank": "开户行",
            "account": "账号",
            "representative": "委托代表人"
        },
        "items": [
            {
                "name": "品名", 
                "spec": "规格", 
                "quantity": 10, 
                "unit": "单位", 
                "price": 100, 
                "remark": "备注"
            }
        ],
        "delivery": {
            "date": "2025-02-01",
            "days": 3
        },
        "quality_requirement": "质量要求",
        "freight_method": "运费方式",
        "payment_method": "付款方式",
        "breach_clause": "违约责任"
    },
    output_path="output.xlsx"
)
```

### 方式二：使用 fill_template_from_dict 函数（GraalVM兼容）

```python
import sys
import os

# GraalVM兼容的路径设置
skills_path = os.environ.get('SKILLS_PATH', '')
if skills_path and skills_path not in sys.path:
    sys.path.insert(0, skills_path)

from xlsx_template_processor import fill_template_from_dict

# 从字典数据填充模板
template_data = {
    "contract_number": "CG-2025-001",
    "sign_date": "2025-01-15",
    "supplier": {
        "name": "供应商名称",
        "legal_person": "法定代表人",
        "phone": "联系电话"
    },
    "items": [
        {"name": "商品1", "quantity": 100, "price": 50},
        {"name": "商品2", "quantity": 50, "price": 100}
    ]
}

result_path = fill_template_from_dict(
    template_path="template.xlsx",
    data=template_data,
    output_path="output.xlsx"
)
```

### 方式三：使用上下文感知的 process_xlsx_template_with_context 函数（推荐用于采购合同，GraalVM兼容）

```python
import sys
import os

# GraalVM兼容的路径设置
skills_path = os.environ.get('SKILLS_PATH', '')
if skills_path and skills_path not in sys.path:
    sys.path.insert(0, skills_path)

from xlsx_template_processor import process_xlsx_template_with_context

# 从用户上下文获取的供应商信息
context_supplier_info = {
    "name": "供应商名称",
    "legal_person": "法定代表人",
    "phone": "联系电话",
    "fax": "传真",
    "bank": "开户行",
    "account": "账号",
    "representative": "委托代表人"
}

# 基础合同数据
basic_data = {
    "contract_number": "CG-2025-001",
    "sign_date": "2025-01-15",
    "items": [
        {
            "name": "品名", 
            "spec": "规格", 
            "quantity": 10, 
            "unit": "单位", 
            "price": 100, 
            "remark": "备注"
        }
    ],
    "delivery": {
        "date": "2025-02-01",
        "days": 3
    },
    "quality_requirement": "质量要求",
    "freight_method": "运费方式",
    "payment_method": "付款方式",
    "breach_clause": "违约责任"
}

# 处理Excel模板，供应商信息优先从上下文获取
result_path = process_xlsx_template_with_context(
    template_path="template.xlsx",
    data=basic_data,
    context_supplier_info=context_supplier_info,  # 从用户上下文获取的信息
    output_path="output.xlsx"
)
```

## 支持的模板类型

### 采购合同模板
- 自动识别采购合同格式
- 专门处理合同编号、供应商信息、商品清单等
- 自动计算总金额并转换为中文大写
- 智能处理交付、质量、付款等条款

### 通用模板
- 支持占位符替换（`{{key}}`格式）
- 支持表格数据动态扩展
- 保留原有格式和样式

## 模板设计指南

### 占位符格式
- 使用 `{{key_name}}` 格式定义占位符
- 例如：`合同编号：{{contract_number}}`

### 表格数据处理
- 模板中的表格区域应有清晰的表头
- 系统会自动识别表头并填充相应的数据行
- 支持动态扩展行数以适应不同数量的数据

## 供应商信息处理策略

### 完整信息模式
当提供完整的供应商信息时，所有字段都会被填充。

### 部分信息模式  
当只提供部分供应商信息时，提供的字段会被填充，缺失的字段将被置空。

### 空信息模式
当不提供供应商信息时，所有供应商相关字段将被置空，等待用户后续填写。

## 依赖

```bash
pip install openpyxl
```

## 测试

本技能包含完整的测试套件，用于验证各项功能：

- `scripts/test_xlsx_template_processor.py`: 基础功能测试
- `scripts/test_context_aware_xlsx_processor.py`: 上下文感知功能测试  
- `scripts/run_tests.py`: 综合测试运行器
- `docs/TESTING.md`: 详细的测试说明文档
- `test/improved_test.py`: 基于实际模板结构的改进测试
- `test/final_validation.py`: 最终验证脚本
- `test/simple_validation.py`: 简化验证脚本

要运行所有测试，请执行：
```bash
cd scripts
python run_tests.py
```

## 注意事项

 1. 模板文件格式需保持稳定
 2. 供应商信息应包含完整字段（使用新函数时）
 3. 商品清单数据应为列表格式
 4. 日期格式支持字符串（YYYY-MM-DD）或datetime对象
 5. 确保模板文件路径正确
 6. 推荐使用 `process_xlsx_template_with_context` 函数处理采购合同，以充分利用上下文感知功能
 7. **GraalVM环境特殊说明**：
    - 在GraalVM Polyglot环境中执行时，`__file__`变量由PythonToolInterceptor自动注入
    - 需要在GraalVM Python环境中安装openpyxl：`graalpy -m pip install openpyxl`
    - 推荐使用环境变量`SKILLS_PATH`来设置模块搜索路径
    - 避免直接使用`os.path.dirname(__file__)`，改用环境变量或绝对路径

## 目录结构

```
xlsx-template-processor/
├── SKILL.md                           # 技能说明文件
├── LICENSE.txt                        # 许可证文件
├── docs/                             # 文档目录
│   ├── README.md                     # 详细使用说明
│   ├── EXAMPLES.md                   # 使用示例
│   ├── TESTING.md                    # 测试说明
│   └── SUMMARY.md                    # 优化总结
├── resource/                         # 资源文件目录
│   └── template.xlsx                 # 示例Excel模板文件
├── scripts/                          # 脚本目录
│   ├── xlsx_template_processor.py    # Excel模板处理主脚本
│   ├── test_xlsx_template_processor.py # 基础测试脚本
│   ├── test_context_aware_xlsx_processor.py # 上下文感知功能测试脚本
│   └── run_tests.py                  # 综合测试运行器
└── test/                             # 测试工具目录
    ├── analyze_template.py           # 模板分析工具
    ├── improved_test.py              # 改进功能测试
    ├── final_validation.py           # 最终验证脚本
    ├── simple_validation.py          # 简化验证脚本
    └── template_analyzer.py          # 模板结构分析工具
```