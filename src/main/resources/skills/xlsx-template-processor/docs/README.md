# Excel模板处理器 (xlsx-template-processor)

这是一个功能强大的Excel模板处理技能，可以根据预定义的Excel模板和输入数据生成新的Excel文件。它特别适用于需要批量生成具有相同格式但不同内容的Excel文件的场景，特别是采购合同等业务文档。

## 特性

- **模板驱动**：基于现有Excel模板进行数据填充
- **智能替换**：自动替换模板中的占位符
- **动态表格**：支持根据数据量动态扩展表格行
- **格式保持**：完全保留原始模板的格式和样式
- **灵活数据结构**：支持简单键值对和复杂嵌套数据结构
- **上下文感知供应商信息处理**：乙方（供方）信息从用户上下文对话中获取，未提供则默认置空

## 使用场景

- 生成采购合同
- 批量制作报表
- 创建标准化文档
- 数据导出到预格式化的Excel文件
- 自动化Excel文档生成

## 安装依赖

```bash
pip install openpyxl
```

## 快速开始

### 1. 准备模板

创建一个Excel模板文件，在需要填充数据的地方使用 `{{placeholder}}` 格式的占位符。

### 2. 准备数据

准备一个包含占位符对应键值的数据字典。

### 3. 生成文件

使用提供的函数处理模板并生成结果文件。

## API接口

### `process_xlsx_template(template_path, data, output_path, start_row_marker=None)`

处理Excel模板并生成新文件。

#### 参数:
- `template_path`: 模板文件路径
- `data`: 包含填充数据的字典
- `output_path`: 输出文件路径
- `start_row_marker`: 表格数据开始标记（可选）

#### 返回:
生成的Excel文件路径

### `fill_template_from_dict(template_path, data, output_path, table_config=None)`

从字典数据填充Excel模板。

#### 参数:
- `template_path`: 模板文件路径
- `data`: 包含填充数据的字典
- `output_path`: 输出文件路径
- `table_config`: 表格配置（可选）

#### 返回:
生成的Excel文件路径

### `process_xlsx_template_with_context(template_path, data, output_path, context_supplier_info=None, start_row_marker=None)` (新增优化功能)

处理Excel模板并生成新文件，支持从用户上下文获取供应商信息。

#### 参数:
- `template_path`: 模板文件路径
- `data`: 包含基础填充数据的字典
- `output_path`: 输出文件路径
- `context_supplier_info`: 从用户上下文获取的供应商信息（可选）
- `start_row_marker`: 表格数据开始标记（可选）

#### 返回:
生成的Excel文件路径

## 新增特性：上下文感知供应商信息处理

本版本的重点优化是实现了上下文感知的供应商信息处理机制：

1. **上下文感知**：供应商信息优先从用户对话上下文中获取
2. **默认置空**：如果用户未提供供应商信息，则将相关字段设置为空白
3. **灵活性增强**：支持部分信息提供，未提供的字段自动置空
4. **兼容性保证**：保持与原有接口的兼容性
5. **模板结构适配**：基于实际模板结构进行精确的数据映射

在采购合同处理逻辑中，对供应商信息的处理进行了以下改进：
- 无论用户是否提供供应商信息，都会尝试从上下文中获取
- 如果上下文中没有相关信息，则将所有供应商字段置为空字符串
- 基于实际模板结构（通过分析确认）进行精确的数据映射
- 保持了原有的业务逻辑结构，同时增强了用户体验

## 实际模板结构分析结果

通过使用xlsx技能分析实际模板文件，我们获得了以下关键信息：
- 工作表名称: 采购合同
- 最大行数: 31
- 最大列数: 13
- 合同编号位置: F2
- 甲方信息位置: B3
- 供应商信息位置: B4, D24-D30
- 商品信息起始行: 第8行
- 签订日期位置: G5

基于这些实际结构信息，我们优化了数据映射逻辑，确保所有数据都精确地填入模板的正确位置。

## 示例

请参见 `scripts/test_xlsx_template_processor.py` 和 `scripts/test_context_aware_xlsx_processor.py` 文件中的具体使用示例。

## 模板设计最佳实践

1. 使用 `{{key_name}}` 格式定义占位符
2. 为表格列设置清晰的表头
3. 保持模板格式的一致性和专业性
4. 在表格区域预留足够的空间以便动态扩展
5. 对于采购合同模板，建议预留供应商信息的固定位置

## 维护

本技能由Taorunhui AI团队维护，如有问题请提交issue。

## 测试和验证

要运行测试，请进入scripts目录：

```bash
cd scripts
python run_tests.py
```

或者运行特定的测试脚本：

```bash
# 基础功能测试
python test_xlsx_template_processor.py

# 上下文感知功能测试
python test_context_aware_xlsx_processor.py

# 运行验证脚本
cd ../test
python simple_validation.py
```

更多使用详情请参阅根目录下的 `USAGE.md` 文件。