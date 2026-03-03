package com.example.springaiapp;

import com.example.springaiapp.skills.ContractGeneratorTool;
import com.example.springaiapp.skills.ContractGeneratorTool.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 合同生成工具测试类
 */
@SpringBootTest
class ContractGeneratorToolTest {

    @TempDir
    Path tempDir;

    @Test
    void testGenerateContract() throws ParseException {
        ContractGeneratorTool tool = new ContractGeneratorTool();
        ContractRequest request = new ContractRequest();
        
        request.setOutputPath("output/测试合同-HT-2025-001.xlsx");
        request.setContractNumber("HT-2025-001");
        request.setSignLocation("湖南醴陵");
        
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        request.setSignDate(sdf.parse("2025-11-29"));
        request.setDeliveryDate(sdf.parse("2025-11-29"));
        request.setDeliveryDays(3);
        request.setDeliveryAddress("湖南省醴陵经济开发区A区陶瓷艺术城釉下五彩生产基地4号");
        request.setDeliveryContact("0731-23676922");
        
        PartyInfo partyA = new PartyInfo();
        partyA.setName("湖南陶润会文化传播有限公司工会委员会");
        request.setPartyA(partyA);
        
        PartyInfo partyB = new PartyInfo();
        partyB.setName("醴陵市时尚灯饰经营部");
        partyB.setLegalRepresentative("张建");
        partyB.setPhone("15273352244");
        partyB.setBank("中国农业银行醴陵市城东支行");
        partyB.setAccount("6228481109443211071");
        request.setPartyB(partyB);
        
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
        
        Product product3 = new Product();
        product3.setName("灯具");
        product3.setSpecification("0.6米白色两线轨道补光灯4000K");
        product3.setQuantity(32);
        product3.setUnit("根");
        product3.setUnitPrice(new BigDecimal("73"));
        products.add(product3);
        
        Product product4 = new Product();
        product4.setName("灯具");
        product4.setSpecification("白色两线轨道直接");
        product4.setQuantity(28);
        product4.setUnit("个");
        product4.setUnitPrice(new BigDecimal("4"));
        products.add(product4);
        
        request.setProducts(products);
        
        String result = tool.apply(request, null);
        System.out.println("合同生成结果:");
        System.out.println(result);
    }

    /**
     * 测试编辑模式：先创建合同，然后编辑
     */
    @Test
    void testEditModeContract() throws ParseException {
        ContractGeneratorTool tool = new ContractGeneratorTool();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        
        File outputFile = tempDir.resolve("edit_test_contract.xlsx").toFile();
        String outputPath = outputFile.getAbsolutePath();
        
        ContractRequest createRequest = new ContractRequest();
        createRequest.setOutputPath(outputPath);
        createRequest.setContractNumber("HT-EDIT-001");
        createRequest.setSignLocation("湖南醴陵");
        createRequest.setSignDate(sdf.parse("2025-03-03"));
        createRequest.setDeliveryDate(sdf.parse("2025-03-10"));
        createRequest.setDeliveryDays(3);
        createRequest.setDeliveryAddress("湖南省醴陵市");
        
        PartyInfo partyA = new PartyInfo();
        partyA.setName("测试甲方公司");
        createRequest.setPartyA(partyA);
        
        PartyInfo partyB = new PartyInfo();
        partyB.setName("测试乙方公司");
        partyB.setPhone("13800138000");
        createRequest.setPartyB(partyB);
        
        List<Product> products = new ArrayList<>();
        Product product = new Product();
        product.setName("测试商品");
        product.setSpecification("初始规格");
        product.setQuantity(10);
        product.setUnit("件");
        product.setUnitPrice(new BigDecimal("100"));
        products.add(product);
        createRequest.setProducts(products);
        
        String createResult = tool.apply(createRequest, null);
        System.out.println("创建合同结果: " + createResult);
        assertTrue(createResult.contains("\"success\": true"), "创建合同应该成功");
        assertTrue(outputFile.exists(), "合同文件应该存在");
        
        long initialSize = outputFile.length();
        System.out.println("初始文件大小: " + initialSize);
        
        ContractRequest editRequest = new ContractRequest();
        editRequest.setOutputPath(outputPath);
        editRequest.setContractNumber("HT-EDIT-001-UPDATED");
        editRequest.setEditMode(true);
        editRequest.setSignLocation("湖南长沙");
        editRequest.setDeliveryDate(sdf.parse("2025-03-15"));
        editRequest.setDeliveryDays(5);
        editRequest.setDeliveryAddress("湖南省长沙市岳麓区");
        
        PartyInfo newPartyA = new PartyInfo();
        newPartyA.setName("修改后的甲方");
        editRequest.setPartyA(newPartyA);
        
        PartyInfo newPartyB = new PartyInfo();
        newPartyB.setName("修改后的乙方");
        newPartyB.setPhone("13900139000");
        editRequest.setPartyB(newPartyB);
        
        List<Product> newProducts = new ArrayList<>();
        Product newProduct1 = new Product();
        newProduct1.setName("修改后的商品1");
        newProduct1.setSpecification("新规格1");
        newProduct1.setQuantity(20);
        newProduct1.setUnit("套");
        newProduct1.setUnitPrice(new BigDecimal("200"));
        newProducts.add(newProduct1);
        
        Product newProduct2 = new Product();
        newProduct2.setName("新增商品2");
        newProduct2.setSpecification("新规格2");
        newProduct2.setQuantity(5);
        newProduct2.setUnit("台");
        newProduct2.setUnitPrice(new BigDecimal("500"));
        newProducts.add(newProduct2);
        editRequest.setProducts(newProducts);
        
        String editResult = tool.apply(editRequest, null);
        System.out.println("编辑合同结果: " + editResult);
        assertTrue(editResult.contains("\"success\": true"), "编辑合同应该成功");
        assertTrue(editResult.contains("\"editMode\": true"), "应该处于编辑模式");
        
        long editedSize = outputFile.length();
        System.out.println("编辑后文件大小: " + editedSize);
        assertTrue(editedSize > 0, "编辑后文件应该有内容");
    }

    /**
     * 测试非编辑模式：文件已存在时应该覆盖
     */
    @Test
    void testOverwriteMode() throws ParseException {
        ContractGeneratorTool tool = new ContractGeneratorTool();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        
        File outputFile = tempDir.resolve("overwrite_test.xlsx").toFile();
        String outputPath = outputFile.getAbsolutePath();
        
        ContractRequest request1 = new ContractRequest();
        request1.setOutputPath(outputPath);
        request1.setContractNumber("HT-OVERWRITE-001");
        
        PartyInfo partyA = new PartyInfo();
        partyA.setName("甲方1");
        request1.setPartyA(partyA);
        
        PartyInfo partyB = new PartyInfo();
        partyB.setName("乙方1");
        request1.setPartyB(partyB);
        
        List<Product> products1 = new ArrayList<>();
        Product product1 = new Product();
        product1.setName("商品1");
        product1.setSpecification("规格1");
        product1.setQuantity(1);
        product1.setUnit("个");
        product1.setUnitPrice(new BigDecimal("10"));
        products1.add(product1);
        request1.setProducts(products1);
        
        String result1 = tool.apply(request1, null);
        assertTrue(result1.contains("\"success\": true"));
        
        ContractRequest request2 = new ContractRequest();
        request2.setOutputPath(outputPath);
        request2.setContractNumber("HT-OVERWRITE-002");
        request2.setEditMode(false);
        
        PartyInfo partyA2 = new PartyInfo();
        partyA2.setName("甲方2");
        request2.setPartyA(partyA2);
        
        PartyInfo partyB2 = new PartyInfo();
        partyB2.setName("乙方2");
        request2.setPartyB(partyB2);
        
        List<Product> products2 = new ArrayList<>();
        Product product2 = new Product();
        product2.setName("商品2");
        product2.setSpecification("规格2");
        product2.setQuantity(2);
        product2.setUnit("个");
        product2.setUnitPrice(new BigDecimal("20"));
        products2.add(product2);
        request2.setProducts(products2);
        
        String result2 = tool.apply(request2, null);
        assertTrue(result2.contains("\"success\": true"));
        assertTrue(result2.contains("\"editMode\": false"), "应该是非编辑模式");
        System.out.println("覆盖模式测试成功");
    }
}
