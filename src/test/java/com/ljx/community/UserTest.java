package com.ljx.community;

import com.ljx.community.entity.User;
import com.ljx.community.service.UserService;
import com.ljx.community.util.CommunityUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

import java.util.Map;

@SpringBootTest
@ContextConfiguration(classes = CommunityWebsiteApplication.class)
public class UserTest {
    @Autowired
    UserService userService;

    @Test
    void md5() {
        System.out.println(CommunityUtil.md5("123"+"93768"));
    }

    @Test
    void registerTest() {
        User user=new User();
        user.setUsername("勇敢牛牛");
        user.setPassword("123456");
        user.setEmail("654@qq.com");
        Map<String, Object> map = userService.register(user);
    }
}
