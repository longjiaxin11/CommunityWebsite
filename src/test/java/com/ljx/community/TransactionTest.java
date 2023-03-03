package com.ljx.community;

import com.ljx.community.service.AlphaService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

@SpringBootTest
@ContextConfiguration(classes = CommunityWebsiteApplication.class)
public class TransactionTest {
    
    @Autowired
    AlphaService alphaService;

    @Test
    void TestSave1() {
        Object savel = alphaService.save1();
        System.out.println(savel);
    }

    @Test
    void TestSave2() {
        Object savel = alphaService.save2();
        System.out.println(savel);
    }
}
