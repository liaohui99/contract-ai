---
name: "contract-analyzer"
description: "智能解析与归档合同文档。上传合同(xlsx/docx)后，AI自动识别并提取关键信息(合同编号、金额、签约方、日期等)，将非结构化文档转化为结构化数据。当用户需要分析合同、提取合同信息或上传合同文件时调用此skill。"
---

# Contract Analyzer - 合同智能解析器

## 概述

本技能用于智能解析合同文档（支持Excel和Word格式），自动识别并提取关键信息，将非结构化文档转化为结构化数据，便于归档和管理。

## 支持的文件格式

| 格式 | 扩展名 | 说明 |
|------|--------|------|
| Excel合同 | .xlsx | Excel格式的合同文件 |
| Word合同 | .docx | Word格式的合同文件 |

## 可提取的关键信息

### 基本信息
- **合同编号**: 合同唯一标识符
- **合同类型**: 采购合同、销售合同、服务合同等
- **签订日期**: 合同签署日期
- **签订地点**: 合同签署地点
- **合同状态**: 有效/过期/待签署

### 当事人信息
- **甲方（买方/需方）**
  - 单位名称
  - 法定代表人
  - 委托代表人
  - 联系电话
  - 开户银行
  - 银行账号

- **乙方（供方/卖方）**
  - 单位名称
  - 法定代表人
  - 委托代表人
  - 联系电话
  - 开户银行
  - 银行账号

### 金额信息
- **合同总金额**: 合同涉及的金额总数
- **金额大写**: 中文大写金额
- **币种**: 人民币/美元等
- **含税情况**: 是否含税

### 商品/服务明细
- **品名**: 商品或服务名称
- **规格型号**: 规格说明
- **数量**: 商品数量
- **单位**: 计量单位
- **单价**: 单位价格
- **金额**: 小计金额
- **备注**: 其他说明

### 条款信息
- **交货时间**: 交货/服务期限
- **交货地点**: 交货地址
- **结算方式**: 付款方式
- **违约责任**: 违约条款
- **争议解决**: 纠纷处理方式

---

## 推荐使用方式：ContractAnalyzerTool

`ContractAnalyzerTool` 是基于本技能开发的合同智能解析工具。

### 工具名称

`contract_analyzer`

### 工具描述

智能解析合同文档，自动识别并提取关键信息。支持Excel(.xlsx)和Word(.docx)格式的合同文件，自动提取合同编号、金额、签约方、日期等关键信息，返回结构化的JSON数据。

### 调用方式

#### 方式1: 作为Spring AI Tool调用（Agent自动调用）

工具已注册为Spring Bean，Agent可自动识别并调用。

#### 方式2: Java代码直接调用

```java
import com.example.springaiapp.skills.ContractAnalyzerTool;
import com.example.springaiapp.skills.ContractAnalyzerTool.*;

// 创建工具实例
ContractAnalyzerTool tool = new ContractAnalyzerTool();

// 构建请求参数
AnalyzeRequest request = new AnalyzeRequest();
request.setFilePath("output/合同-HT-2025-001.xlsx");

// 分析合同
AnalyzeResult result = tool.analyze(request);
System.out.println(result);
```

### 请求参数说明

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| filePath | String | 是 | 合同文件的完整路径 |

### 返回结果示例

```json
{
  "success": true,
  "filePath": "output/合同-HT-2025-001.xlsx",
  "fileType": "xlsx",
  "contractInfo": {
    "contractNumber": "HT-2025-001",
    "contractType": "采购合同",
    "signDate": "2025-11-29",
    "signLocation": "湖南醴陵",
    "partyA": {
      "name": "湖南陶润会文化传播有限公司工会委员会",
      "legalRepresentative": null,
      "phone": null,
      "bank": null,
      "account": null
    },
    "partyB": {
      "name": "醴陵市时尚灯饰经营部",
      "legalRepresentative": "张建",
      "phone": "15273352244",
      "bank": "中国农业银行醴陵市城东支行",
      "account": "6228481109443211071"
    },
    "totalAmount": 6352.00,
    "amountInWords": "陆仟叁佰伍拾贰元整",
    "deliveryDate": "2025-11-29",
    "deliveryDays": 3,
    "deliveryAddress": "湖南省醴陵经济开发区A区陶瓷艺术城釉下五彩生产基地4号",
    "deliveryContact": "0731-23676922",
    "products": [
      {
        "name": "灯具",
        "specification": "加厚白色两线轨道条",
        "quantity": 64,
        "unit": "米",
        "unitPrice": 23.00,
        "amount": 1472.00,
        "remark": null
      },
      {
        "name": "灯具",
        "specification": "白色两线轨道射灯4000K",
        "quantity": 32,
        "unit": "盏",
        "unitPrice": 76.00,
        "amount": 2432.00,
        "remark": null
      }
    ],
    "terms": {
      "qualityRequirement": "按国家标准执行",
      "freightMethod": "乙方负责",
      "paymentMethod": "先付款后发货（乙方提供1%增值税普票）",
      "liability": "乙方必须按合同要求时间交货，如未按期交货，予以赔偿延期所致甲方的损失",
      "disputeResolution": "协商不成，向甲方所在地人民法院提起诉讼"
    }
  },
  "extractedAt": "2025-03-04T11:00:00"
}
```

---

## 核心功能

### 1. 智能识别

自动识别合同文档类型和格式：
- Excel合同：识别表头、数据区域、合并单元格
- Word合同：识别段落、表格、标题结构

### 2. 信息提取

基于规则和模式匹配提取关键信息：
- 合同编号识别（如：HT-2025-001）
- 日期格式识别（如：2025年11月29日）
- 金额识别（数字和中文大写）
- 当事人信息识别（甲方/乙方）

### 3. 数据结构化

将非结构化文档转化为结构化JSON数据：
- 统一的数据格式
- 完整的信息字段
- 便于存储和检索

---

## 使用场景

1. **合同归档**: 上传合同后自动提取信息，便于归档管理
2. **信息核对**: 快速核对合同关键信息是否完整
3. **数据迁移**: 将纸质或扫描合同数字化后提取信息
4. **合同审核**: 辅助审核人员快速定位关键条款
5. **报表统计**: 提取数据用于统计分析

---

## 相关工具类

- [ContractAnalyzerTool](file:///e:/study/AI/taorunhui-ai/src/main/java/com/example/springaiapp/skills/ContractAnalyzerTool.java) - 合同智能解析工具

## 相关技能

- [template-parser](file:///e:/study/AI/taorunhui-ai/.trae/skills/template-parser/SKILL.md) - 合同模版解析器
- [xlsx_tool](file:///e:/study/AI/taorunhui-ai/src/main/resources/skills/xlsx/SKILL.md) - Excel操作技能
- [docx_tool](file:///e:/study/AI/taorunhui-ai/src/main/resources/skills/docx/SKILL.md) - Word操作技能
