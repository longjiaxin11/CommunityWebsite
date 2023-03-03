package com.ljx.community;

import com.ljx.community.dao.AlphaDao;
import org.junit.jupiter.api.Test;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.test.context.ContextConfiguration;

import java.text.SimpleDateFormat;
import java.util.Date;

@SpringBootTest
@ContextConfiguration(classes = CommunityWebsiteApplication.class)
class CommunityWebsiteApplicationTests implements ApplicationContextAware {

    private ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext=applicationContext;
    }

    @Test
    void applicationTest() {
        //System.out.println(applicationContext);
        AlphaDao alphaDao = applicationContext.getBean(AlphaDao.class);
        //AlphaDao alphaDao = applicationContext.getBean("hibernate",AlphaDao.class);
        System.out.println(alphaDao.select());
    }

    @Test
    void testBeanManegement() {
        //AlphaService alphaService = applicationContext.getBean(AlphaService.class);
    }

    @Test
    void TestBeanConfig() {
        SimpleDateFormat simpleDateFormat = applicationContext.getBean(SimpleDateFormat.class);
        System.out.println(simpleDateFormat.format(new Date()));
    }

    @Autowired
    @Qualifier("mybatis")
    private AlphaDao alphaDao;


    @Test
    void TestDI() {
        System.out.println(alphaDao);
    }
}
