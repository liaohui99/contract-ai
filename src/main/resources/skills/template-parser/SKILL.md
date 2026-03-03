---
name: template-parser
description: 精准读取并解析合同模版文件，提取必要值及样式信息。当用户需要分析合同模版、生成新合同或处理Excel模版时调用此skill。
---

# Template Parser - 合同模版解析器

## 概述

本技能定义了采购合同的标准格式和样式规范，用于指导合同生成工具创建符合规范的采购合同Excel文件。

**重要说明**：本技能不依赖任何外部模版文件，所有合同均通过 `ContractGeneratorTool` 从零创建，完全基于本技能定义的格式和样式规范。
甲方公司名称如果没有特殊说明，则使用默认值:'湖南陶润会文化传播有限公司工会委员会'。

## 合同格式规范

### 一、表头区域（第1-2行）

#### 第1行（索引0）- 公司名称
- **单元格**: A1:H1（合并单元格）
- **内容**: 甲方公司名称
- **样式**: 居中对齐，加粗字体，16号宋体

#### 第2行（索引1）- 合同标题
- **单元格**: A2:H2（合并单元格）
- **内容**: `采购合同`
- **样式**: 居中对齐，大号字体，加粗，16号宋体

### 二、基本信息区域（第3-5行）

#### 第3行（索引2）- 甲方信息
| 单元格 | 内容 | 说明 |
|--------|------|------|
| A3 | `甲方（买方）：` | 固定文本 |
| B3:D3 | 甲方名称 | 可变字段（合并单元格） |
| E3:F3 | `合同编号：` | 固定文本（合并单元格） |
| G3:H3 | 合同编号 | 可变字段（合并单元格） |

#### 第4行（索引3）- 签订地点
| 单元格 | 内容 | 说明 |
|--------|------|------|
| E4:F4 | `签订地点：` | 固定文本（合并单元格） |
| G4:H4 | 签订地点 | 可变字段（合并单元格） |

#### 第5行（索引4）- 乙方信息及签订时间
| 单元格 | 内容 | 说明 |
|--------|------|------|
| A5 | `乙方（供方）：` | 固定文本 |
| B5:D5 | 乙方名称 | 可变字段（合并单元格） |
| E5:F5 | `签订时间：` | 固定文本（合并单元格） |
| G5:H5 | 签订时间 | 可变字段（合并单元格） |

### 三、商品明细表区域（第6-13行）

#### 第6行（索引5）- 表格标题
- **单元格**: A6:H6（合并单元格）
- **内容**: `一、购销物品项及数量价款：`
- **样式**: 左对齐，加粗

#### 第7行（索引6）- 表头
| 列 | 单元格 | 内容 | 说明 |
|----|--------|------|------|
| A | A7 | `甲方销售合同号` | 表头 |
| B | B7 | `品名` | 表头 |
| C | C7 | `规格型号` | 表头 |
| D | D7 | `数量` | 表头 |
| E | E7 | `单位` | 表头 |
| F | F7 | `单价` | 表头 |
| G | G7 | `含税金额` | 表头 |
| H | H7 | `备注` | 表头 |

#### 第8-N行（索引7+）- 商品数据行
| 列 | 字段名 | 数据类型 | 说明 |
|----|--------|----------|------|
| A | 甲方销售合同号 | String | 可选 |
| B | 品名 | String | 必填 |
| C | 规格型号 | String | 必填 |
| D | 数量 | Number | 必填 |
| E | 单位 | String | 必填 |
| F | 单价 | Number | 必填 |
| G | 含税金额 | Number | 必填（自动计算：数量×单价） |
| H | 备注 | String | 可选 |

#### 合计行 - 数量和金额合计
| 单元格 | 内容 | 说明 |
|--------|------|------|
| A | `合计` | 固定文本 |
| D | 数量合计 | 所有商品数量之和 |
| G | 金额合计 | 所有商品含税金额之和 |

#### 金额大写行
| 单元格 | 内容 | 说明 |
|--------|------|------|
| A | `（人民币）` | 固定文本 |
| B:H | 金额大写 | 金额合计的中文大写（合并单元格） |

### 四、合同条款区域（第14-22行）

每行都是A:H列合并单元格，包含以下条款：

1. **二、交货时间**：交货日期及货运天数
2. **三、交货地点**：交货地址及联系电话
3. **四、技术标准、质量要求**：
4. **五、运费方式及费用承担**：乙方负责
5. **六、结算及支付方式**：先付款后发货（乙方提供1%增值税普票）
6. **七、违约责任**：乙方必须按合同要求时间交货，如未按期交货，予以赔偿延期所致甲方的损失
7. **八、本合同未尽事宜**：由双方协商解决，必要时以合同附件形式另行签订
8. **九、争议解决**：协商不成，向甲方所在地人民法院提起诉讼
9. **十、合同份数**：一式贰份，双方各执一份

### 五、签章区域（第24-31行）

包含购货方（甲方）、供货方（乙方）和公证栏三部分：

#### 购货方（甲方）信息
- 单位名称（盖章）
- 法定代表人
- 委托代表人
- 电话
- 传真
- 开户行
- 账号

#### 供货方（乙方）信息
- 单位名称（盖章）
- 法定代表人
- 委托代表人
- 电话
- 传真
- 开户行
- 账号

#### 公证栏信息
- 签（公）证意见
- 经办人
- 签（公）证机关（章）
- 日期
- 备注

## 样式规范

### 列宽设置

| 列 | 宽度（像素） | 说明 |
|----|-------------|------|
| A | 3584 | 甲方销售合同号 |
| B | 5888 | 品名 |
| C | 7168 | 规格型号 |
| D | 2560 | 数量 |
| E | 2048 | 单位 |
| F | 2560 | 单价 |
| G | 3840 | 含税金额 |
| H | 4096 | 备注 |

### 样式特征

#### 标题样式
- **字体**: 宋体
- **字号**: 16号
- **对齐**: 居中对齐
- **加粗**: 是

#### 表头样式
- **字体**: 宋体
- **字号**: 11号
- **对齐**: 居中对齐
- **加粗**: 是
- **背景色**: 浅灰色
- **边框**: 四边细线

#### 数据区域样式
- **字体**: 宋体
- **字号**: 10号
- **对齐**: 居中对齐
- **边框**: 四边细线

#### 条款样式
- **字体**: 宋体
- **字号**: 10号
- **对齐**: 左对齐
- **自动换行**: 是

---

## 推荐使用方式：ContractGeneratorTool

`ContractGeneratorTool` 是基于本技能定义的格式和样式开发的专用合同生成工具，**推荐优先使用**。

### 工具名称

`contract_generator`

### 工具描述

生成采购合同Excel文件。**完全从零创建**，不依赖任何模版文件。根据本技能定义的格式和样式自动创建完整的合同结构，自动填充合同信息、商品明细，并计算合计金额。支持金额大写转换。

### 调用方式

#### 方式1: 作为Spring AI Tool调用（Agent自动调用）

工具已注册为Spring Bean，Agent可自动识别并调用。

#### 方式2: Java代码直接调用

```java
import com.example.springaiapp.skills.ContractGeneratorTool;
import com.example.springaiapp.skills.ContractGeneratorTool.*;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;

// 创建工具实例
ContractGeneratorTool tool = new ContractGeneratorTool();

// 构建请求参数
ContractRequest request = new ContractRequest();
request.setOutputPath("output/合同-HT-2025-001.xlsx");
request.setContractNumber("HT-2025-001");
request.setSignLocation("湖南醴陵");

// 设置签订日期
SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
request.setSignDate(sdf.parse("2025-11-29"));

// 设置交货信息
request.setDeliveryDate(sdf.parse("2025-11-29"));
request.setDeliveryDays(3);
request.setDeliveryAddress("湖南省醴陵经济开发区A区陶瓷艺术城釉下五彩生产基地4号");
request.setDeliveryContact("0731-23676922");

// 设置甲方信息
PartyInfo partyA = new PartyInfo();
partyA.setName("湖南陶润会文化传播有限公司工会委员会");
request.setPartyA(partyA);

// 设置乙方信息
PartyInfo partyB = new PartyInfo();
partyB.setName("醴陵市时尚灯饰经营部");
partyB.setLegalRepresentative("张建");
partyB.setPhone("15273352244");
partyB.setBank("中国农业银行醴陵市城东支行");
partyB.setAccount("6228481109443211071");
request.setPartyB(partyB);

// 设置商品明细
List<Product> products = new ArrayList<>();

Product product1 = new Product();
product1.setName("灯具");
product1.setSpecification("加厚白色两线轨道条");
product1.setQuantity(64);
product1.setUnit("米");
product1.setUnitPrice(new BigDecimal("23"));
products.add(product1);

Product product2 = new Product();
product2.setName("灯具");
product2.setSpecification("白色两线轨道射灯4000K");
product2.setQuantity(32);
product2.setUnit("盏");
product2.setUnitPrice(new BigDecimal("76"));
products.add(product2);

request.setProducts(products);

// 生成合同
String result = tool.apply(request, null);
System.out.println(result);
```

### 请求参数说明

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| outputPath | String | 是 | 输出文件路径，如: `output/合同-HT-2025-001.xlsx` |
| contractNumber | String | 是 | 合同编号，如: `HT-2025-001` |
| partyA | PartyInfo | 是 | 甲方（买方）信息 |
| partyB | PartyInfo | 是 | 乙方（供方）信息 |
| products | List\<Product\> | 是 | 商品明细列表 |
| signLocation | String | 否 | 签订地点，默认: 湖南醴陵 |
| signDate | Date | 否 | 签订时间 |
| deliveryDate | Date | 否 | 交货时间 |
| deliveryDays | Integer | 否 | 货运天数 |
| deliveryAddress | String | 否 | 交货地点 |
| deliveryContact | String | 否 | 交货联系电话 |

### PartyInfo 当事人信息

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| name | String | 是 | 单位名称 |
| legalRepresentative | String | 否 | 法定代表人 |
| delegate | String | 否 | 委托代表人 |
| phone | String | 否 | 联系电话 |
| fax | String | 否 | 传真号码 |
| bank | String | 否 | 开户银行 |
| account | String | 否 | 银行账号 |

### Product 商品信息

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| contractNumber | String | 否 | 甲方销售合同号 |
| name | String | 是 | 品名 |
| specification | String | 是 | 规格型号 |
| quantity | Integer | 是 | 数量 |
| unit | String | 是 | 单位 |
| unitPrice | BigDecimal | 是 | 单价 |
| amount | BigDecimal | 否 | 含税金额（不填则自动计算：数量×单价） |
| remark | String | 否 | 备注 |

### 返回结果示例

```json
{
  "success": true,
  "outputPath": "output/合同-HT-2025-001.xlsx",
  "contractNumber": "HT-2025-001",
  "productCount": 4,
  "totalAmount": 6352,
  "message": "采购合同生成成功"
}
```

### 自动计算功能

工具会自动完成以下计算：

1. **含税金额**: 若未提供，自动计算 `数量 × 单价`
2. **数量合计**: 所有商品数量之和
3. **金额合计**: 所有商品含税金额之和
4. **金额大写**: 将金额合计转换为中文大写（如：陆仟叁佰伍拾貮元整）

### 核心优势

1. **不依赖模版文件** - 完全从零创建，无需外部模版
2. **完美保留样式** - 基于本技能定义的格式和样式规范
3. **自动计算** - 无需手动计算合计和金额大写
4. **类型安全** - 使用强类型参数，避免单元格位置错误
5. **简单易用** - 面向对象API，无需记忆单元格位置

---

## 相关工具类

- [ContractGeneratorTool](file:///e:/study/AI/taorunhui-ai/src/main/java/com/example/springaiapp/skills/ContractGeneratorTool.java) - **推荐使用** 合同生成工具（基于本技能定义的格式）

## 测试类

- [ContractGeneratorToolTest](file:///e:/study/AI/taorunhui-ai/src/test/java/com/example/springaiapp/ContractGeneratorToolTest.java) - 合同生成工具测试
