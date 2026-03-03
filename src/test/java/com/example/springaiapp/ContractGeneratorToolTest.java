package com.example.springaiapp;

import com.example.springaiapp.skills.ContractGeneratorTool;
import com.example.springaiapp.skills.ContractGeneratorTool.*;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * 合同生成工具测试类
 */
@SpringBootTest
class ContractGeneratorToolTest {

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
}
