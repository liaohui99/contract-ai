# 测试说明文档

## 测试脚本概述

xlsx-template-processor 技能包含多个测试脚本，用于验证不同方面的功能。这些测试脚本主要用于开发和质量保证阶段，确保技能的功能正常。

## 测试脚本清单

### 1. test_xlsx_template_processor.py
- **位置**: `scripts/test_xlsx_template_processor.py`
- **功能**: 验证基础功能，包括：
  - 基本模板处理功能
  - 采购合同处理功能
  - 高级功能测试
  - 用户输入集成测试

### 2. test_context_aware_xlsx_processor.py
- **位置**: `scripts/test_context_aware_xlsx_processor.py`
- **功能**: 验证上下文感知的供应商信息处理功能，包括：
  - 上下文感知供应商信息功能
  - 空供应商上下文情况
  - 部分供应商上下文情况

### 3. run_tests.py
- **位置**: `run_tests.py`
- **功能**: 整合测试脚本，运行所有测试并提供统一的结果报告

## 如何运行测试

### 方法一：运行单个测试脚本
```bash
cd xlsx-template-processor
python scripts/test_xlsx_template_processor.py
```

```bash
cd xlsx-template-processor
python scripts/test_context_aware_xlsx_processor.py
```

### 方法二：运行综合测试
```bash
cd xlsx-template-processor
python run_tests.py
```

## 测试目的

这些测试脚本的主要目的是：

1. **功能验证**：确保技能的核心功能按预期工作
2. **回归测试**：在代码更改后验证功能未被破坏
3. **边界条件测试**：验证在极端情况下（如空输入、部分输入）的行为
4. **集成测试**：验证不同组件之间的交互

## 测试覆盖范围

### 基础功能测试
- Excel模板加载和保存
- 占位符替换
- 表格数据处理
- 日期和数值格式化

### 采购合同专项测试
- 合同编号处理
- 供应商信息处理
- 商品清单处理
- 金额中文转换

### 上下文感知功能测试
- 供应商信息从上下文获取
- 空上下文处理（置空功能）
- 部分上下文处理
- 数据合并逻辑

## 测试环境要求

- Python 3.7+
- openpyxl 库 (`pip install openpyxl`)
- Excel模板文件（resource/template.xlsx）

## 何时运行测试

1. **开发阶段**：在每次代码更改后运行相关测试
2. **发布前**：运行所有测试确保功能完整性
3. **集成前**：在与其他模块集成前验证功能
4. **定期维护**：定期运行测试确保长期稳定性

## 测试结果解读

- `[SUCCESS]`：测试通过
- `[FAILED]`：测试失败，需要调查原因
- `[PASSED]`：功能测试通过
- `[ALL TESTS PASSED]`：所有测试均通过

## 维护说明

- 添加新功能时，应相应增加测试用例
- 修复bug时，应添加回归测试
- 测试脚本本身也需要维护，确保与主功能同步更新