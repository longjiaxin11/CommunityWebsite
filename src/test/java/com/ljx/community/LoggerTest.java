package com.ljx.community;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

@SpringBootTest
@ContextConfiguration(classes = CommunityWebsiteApplication.class)
public class LoggerTest {

    private static final Logger logger = LoggerFactory.getLogger(LoggerTest.class);

    @Test
    void testLogger() {
        System.out.println(logger.getName());

        logger.debug("debug log");
        logger.debug("info log");
        logger.debug("warn log");
        logger.debug("error log");
    }

    @Test
    void test1() {
        String s="测试git";
    }
}
