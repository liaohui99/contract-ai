# ExcelWriteTool 覆盖问题修正计划

## 问题分析

### 问题现象
AI在编辑已生成的合同时，错误地使用了 `excel_write` 工具，导致原始合同内容被清空，只保留了新写入的一行数据。

### 根本原因
1. **工具描述不够清晰**：描述中 `data数据列表(必填,二维数组)` 误导AI认为必须提供 `data` 参数
2. **工具逻辑存在缺陷**：当 `filePath` 指向已存在的文件时，工具没有自动保护原有内容
3. **缺少安全机制**：没有防止意外覆盖已存在文件的保护措施

### 问题代码位置
- [ExcelWriteTool.java:101-125](file:///e:/study/AI/taorunhui-ai/src/main/java/com/example/springaiapp/skills/ExcelWriteTool.java#L101-L125) - writeExcel方法中的覆盖逻辑
- [ExcelWriteTool.java:372-380](file:///e:/study/AI/taorunhui-ai/src/main/java/com/example/springaiapp/skills/ExcelWriteTool.java#L372-L380) - 工具描述

## 修正方案

### 方案选择：修改工具逻辑 + 优化描述

**核心思路**：当检测到目标文件已存在且没有提供 `templatePath` 或 `appendMode` 时，自动将原文件作为模板处理，保护原有内容。

### 实施步骤

#### 步骤1：修改 apply 方法逻辑
在 `ExcelWriteTool.apply()` 方法中添加智能判断：
- 如果 `filePath` 指向已存在的文件
- 且没有提供 `templatePath`
- 且没有设置 `appendMode=true`
- 则自动将 `filePath` 作为 `templatePath` 使用

#### 步骤2：修改 writeExcel 方法
调整 `writeExcel()` 方法，在覆盖模式下检测文件是否存在：
```java
if (!appendMode && templatePath == null) {
    java.io.File file = new java.io.File(filePath);
    if (file.exists() && file.length() > 0) {
        // 自动使用原文件作为模板，保护原有内容
        templatePath = filePath;
        log.info("检测到文件已存在，自动使用原文件作为模板以保护原有内容");
    }
}
```

#### 步骤3：优化工具描述
更新 `createToolCallback()` 中的描述，使其更清晰：
- 明确说明编辑现有文件的推荐方式
- 说明 `data` 参数在提供 `cellMapping` 时不是必填的
- 添加安全机制说明

#### 步骤4：添加新的参数说明
为 `WriteRequest` 类添加更详细的参数说明，特别是：
- `editMode` 参数说明（可选，用于明确表示编辑模式）
- 更新 `data` 参数说明为"可选"

#### 步骤5：编写单元测试
添加测试用例验证：
- 编辑已存在文件时原有内容被保留
- 新建文件时正常工作
- appendMode 正常工作
- templatePath + cellMapping 组合正常工作

## 代码修改清单

| 文件 | 修改内容 |
|------|----------|
| ExcelWriteTool.java | 修改 apply() 方法添加智能判断逻辑 |
| ExcelWriteTool.java | 修改 writeExcel() 方法处理自动模板逻辑 |
| ExcelWriteTool.java | 更新工具描述使其更清晰 |
| ExcelWriteTool.java | 更新 WriteRequest 参数说明 |
| ExcelWriteToolTest.java | 添加编辑模式测试用例 |

## 预期效果

修正后：
1. AI调用 `excel_write` 编辑已存在的文件时，原有内容会被自动保护
2. 工具描述更清晰，AI能更好地理解如何正确使用
3. 防止意外覆盖已存在的文件内容
