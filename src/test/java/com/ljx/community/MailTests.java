package com.ljx.community;


import com.ljx.community.util.MailClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@SpringBootTest
@ContextConfiguration(classes = CommunityWebsiteApplication.class)
public class MailTests {
    @Autowired
    MailClient mailClient;

    @Autowired
    private TemplateEngine templateEngine;

    @Test
    void TestTestEmail() {
        mailClient.sendMail("1452917134@qq.com","Test1","Hello Email!");
    }

    @Test
    void testHtmlEmail() {
        Context context=new Context();
        context.setVariable("username","龙佳鑫");
        String content = templateEngine.process("/mail/testEmail", context);
        System.out.println(content);
        mailClient.sendMail("1452917134@qq.com","TestHtml",content);
    }
}
