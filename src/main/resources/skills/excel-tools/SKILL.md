---
name: excel_tools
description: "Excel文件处理工具集，包含读取、写入、分析和生成四个核心工具。当用户需要处理.xlsx格式文件时调用此技能。支持读取Excel数据、写入新数据、分析文件结构、以及基于模板AI生成新文件。适用于数据分析、报表生成、模板填充等场景。"
license: Proprietary
---

# Excel工具集操作手册

本技能集提供四个核心Excel处理工具，供Agent调用完成各类Excel操作任务。

## 工具概览

| 工具名称 | 功能描述 | 典型场景 |
|---------|---------|---------|
| excel_read | 读取Excel文件内容 | 数据导入、内容查看 |
| excel_write | 写入数据到Excel文件 | 数据导出、报表生成 |
| excel_analysis | 分析Excel文件结构 | 了解文件结构、数据统计 |
| excel_generator | 基于模板AI生成Excel | 智能填充、批量生成 |

---

## 一、excel_read - Excel读取工具

### 功能说明
读取Excel文件内容，支持指定工作表、行列范围，自动识别表头。

### 参数说明

| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| filePath | String | 是 | - | Excel文件的完整路径 |
| sheetName | String | 否 | 第一个工作表 | 工作表名称 |
| hasHeader | Boolean | 否 | true | 是否包含表头 |
| startRow | Integer | 否 | 0 | 起始行号(从0开始) |
| endRow | Integer | 否 | -1 | 结束行号(-1表示到最后) |
| startCol | Integer | 否 | 0 | 起始列号(从0开始) |
| endCol | Integer | 否 | -1 | 结束列号(-1表示到最后) |

### 返回格式

```json
{
  "success": true,
  "filePath": "文件路径",
  "totalRows": 10,
  "data": [
    {"姓名": "张三", "年龄": 25, "城市": "北京"},
    {"姓名": "李四", "年龄": 30, "城市": "上海"}
  ]
}
```

### 使用示例

**场景1：读取整个文件**
```json
{
  "filePath": "E:/data/employees.xlsx"
}
```

**场景2：读取指定工作表**
```json
{
  "filePath": "E:/data/report.xlsx",
  "sheetName": "销售数据"
}
```

**场景3：读取指定范围**
```json
{
  "filePath": "E:/data/sales.xlsx",
  "startRow": 2,
  "endRow": 50,
  "startCol": 0,
  "endCol": 5
}
```

**场景4：读取无表头数据**
```json
{
  "filePath": "E:/data/raw_data.xlsx",
  "hasHeader": false
}
```

---

## 二、excel_write - Excel写入工具

### 功能说明
将数据写入Excel文件，支持自定义表头、追加模式和覆盖模式。

### 参数说明

| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| filePath | String | 是 | - | 输出Excel文件的完整路径 |
| sheetName | String | 否 | Sheet1 | 工作表名称 |
| headers | List<String> | 否 | - | 表头列表 |
| data | List<List<Object>> | 是 | - | 数据列表(二维数组) |
| appendMode | Boolean | 否 | false | 是否追加模式 |

### 返回格式

```json
{
  "success": true,
  "filePath": "输出文件路径",
  "sheetName": "Sheet1",
  "totalRows": 10,
  "message": "Excel文件写入成功"
}
```

### 使用示例

**场景1：创建新文件**
```json
{
  "filePath": "E:/output/result.xlsx",
  "headers": ["姓名", "年龄", "城市"],
  "data": [
    ["张三", 25, "北京"],
    ["李四", 30, "上海"],
    ["王五", 28, "广州"]
  ]
}
```

**场景2：追加数据到现有文件**
```json
{
  "filePath": "E:/output/result.xlsx",
  "sheetName": "Sheet1",
  "data": [
    ["赵六", 35, "深圳"],
    ["钱七", 22, "杭州"]
  ],
  "appendMode": true
}
```

**场景3：写入无表头数据**
```json
{
  "filePath": "E:/output/data.xlsx",
  "data": [
    [1, "产品A", 100],
    [2, "产品B", 200]
  ]
}
```

---

## 三、excel_analysis - Excel分析工具

### 功能说明
分析Excel文件结构信息，包括工作表信息、列统计、数据类型分布等。

### 参数说明

| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| filePath | String | 是 | - | Excel文件的完整路径 |
| analysisType | String | 否 | full | 分析类型: full/structure/data |
| sheetName | String | 否 | 全部分析 | 目标工作表名称 |

### 分析类型说明

| 类型 | 说明 |
|------|------|
| full | 完整分析：文件信息+结构+数据统计 |
| structure | 仅结构：工作表数量、行列数、表头 |
| data | 数据统计：列统计、类型分布、空值统计 |

### 返回格式

```json
{
  "success": true,
  "filePath": "文件路径",
  "fileSize": "125.5 KB",
  "totalSheets": 2,
  "sheets": [
    {
      "sheetName": "Sheet1",
      "sheetIndex": 0,
      "totalRows": 100,
      "maxColumns": 5,
      "headers": ["姓名", "年龄", "城市", "入职日期", "薪资"],
      "dataRows": 99,
      "emptyRows": 0,
      "columnStats": [
        {
          "columnIndex": 0,
          "nonEmptyCount": 99,
          "uniqueValueCount": 95,
          "inferredType": "STRING",
          "sampleValues": ["张三", "李四", "王五"]
        },
        {
          "columnIndex": 1,
          "nonEmptyCount": 99,
          "numericCount": 99,
          "minValue": 22,
          "maxValue": 55,
          "avgValue": 32.5,
          "inferredType": "NUMERIC"
        }
      ],
      "cellTypeDistribution": {
        "STRING": 198,
        "NUMERIC": 198,
        "DATE": 99,
        "BLANK": 5
      }
    }
  ]
}
```

### 使用示例

**场景1：完整分析**
```json
{
  "filePath": "E:/data/employees.xlsx"
}
```

**场景2：仅分析结构**
```json
{
  "filePath": "E:/data/report.xlsx",
  "analysisType": "structure"
}
```

**场景3：分析指定工作表的数据统计**
```json
{
  "filePath": "E:/data/sales.xlsx",
  "analysisType": "data",
  "sheetName": "2024年销售"
}
```

---

## 四、excel_generator - Excel生成工具

### 功能说明
分析模板Excel结构，结合用户需求通过AI生成新数据并写入新文件。

### 参数说明

| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| templatePath | String | 是 | - | 模板Excel文件路径 |
| outputPath | String | 是 | - | 输出Excel文件路径 |
| userContext | String | 是 | - | 用户需求描述 |
| sheetName | String | 否 | 第一个工作表 | 工作表名称 |
| generateRows | Integer | 否 | 10 | 生成数据行数 |

### 返回格式

```json
{
  "success": true,
  "templatePath": "模板路径",
  "outputPath": "输出路径",
  "sheetName": "Sheet1",
  "headers": ["姓名", "年龄", "城市"],
  "generatedRows": 10,
  "message": "Excel文件生成成功"
}
```

### 使用示例

**场景1：生成员工信息**
```json
{
  "templatePath": "E:/templates/employee_template.xlsx",
  "outputPath": "E:/output/employees_new.xlsx",
  "userContext": "生成10条员工信息，姓名为中文，年龄在25-40岁之间，城市为中国一线城市",
  "generateRows": 10
}
```

**场景2：生成销售数据**
```json
{
  "templatePath": "E:/templates/sales_template.xlsx",
  "outputPath": "E:/output/sales_2024.xlsx",
  "userContext": "生成2024年1月至12月的销售数据，产品名称为电子产品，销售额在10000-50000之间",
  "generateRows": 12
}
```

**场景3：生成测试数据**
```json
{
  "templatePath": "E:/templates/test_template.xlsx",
  "outputPath": "E:/output/test_data.xlsx",
  "userContext": "生成测试数据，包含用户名(英文)、密码(随机字符串)、邮箱(有效格式)、手机号(11位数字)",
  "generateRows": 50
}
```

---

## 典型工作流程

### 流程1：读取并分析数据

```
1. 调用 excel_analysis 分析文件结构
   ↓ 了解工作表、列名、数据类型
   
2. 调用 excel_read 读取需要的数据
   ↓ 根据分析结果精确读取
```

### 流程2：数据处理并导出

```
1. 调用 excel_read 读取源数据
   ↓ 获取原始数据
   
2. (Agent内部处理数据)
   ↓ 数据清洗、转换、计算
   
3. 调用 excel_write 写入结果
   ↓ 导出处理后的数据
```

### 流程3：基于模板生成新文件

```
1. 调用 excel_analysis 分析模板结构
   ↓ 了解模板列信息
   
2. 调用 excel_generator 生成新文件
   ↓ AI根据用户需求生成数据
```

### 流程4：批量追加数据

```
1. 调用 excel_analysis 检查目标文件结构
   ↓ 确认列数和格式
   
2. 调用 excel_write 追加数据
   ↓ appendMode=true
```

---

## 注意事项

### 文件格式
- 所有工具仅支持 `.xlsx` 格式（Excel 2007+）
- 不支持 `.xls`、`.csv` 等其他格式

### 路径要求
- 文件路径必须为绝对路径
- Windows路径使用正斜杠 `/` 或双反斜杠 `\\`

### 数据类型支持
- 字符串 (String)
- 数字 (Number)
- 布尔值 (Boolean)
- 日期 (Date)
- 公式 (Formula，读取时返回计算结果)

### 性能建议
- 大文件读取建议指定行列范围
- 追加模式比覆盖模式更高效
- 分析大文件时使用 `structure` 类型更快

---

## 错误处理

所有工具返回统一的错误格式：

```json
{
  "success": false,
  "error": "错误描述信息"
}
```

常见错误：
- 文件不存在：检查路径是否正确
- 工作表不存在：检查sheetName参数
- 权限不足：检查文件读写权限
- 格式错误：确认文件为有效的xlsx格式

---

## 版本信息

- 版本：1.0.0
- 依赖：Apache POI 5.2.5
- 支持的Java版本：21+
