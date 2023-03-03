package com.ljx.community;

import com.ljx.community.dao.DiscussPostMapper;
import com.ljx.community.dao.LoginTicketMapper;
import com.ljx.community.dao.MessageMapper;
import com.ljx.community.dao.UserMapper;
import com.ljx.community.entity.DiscussPost;
import com.ljx.community.entity.LoginTicket;
import com.ljx.community.entity.Message;
import com.ljx.community.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

import java.util.Date;
import java.util.List;

@SpringBootTest
@ContextConfiguration(classes = CommunityWebsiteApplication.class)
public class MapperTest {

    @Autowired
    UserMapper userMapper;

    @Autowired
    DiscussPostMapper discussPostMapper;

    @Autowired
    LoginTicketMapper loginTicketMapper;

    @Autowired
    MessageMapper messageMapper;

    @Test
    void testSelect() {
        System.out.println(userMapper.selectById(125));
        System.out.println(userMapper.selectByName("ljx"));
        System.out.println(userMapper.selectByEmail("1452917134@qq.com"));
    }

    @Test
    void testInsert() {
        User user = new User();
        user.setUsername("ljx");
        user.setPassword("123456");
        user.setSalt("hello");
        user.setEmail("1452917134@qq.com");
        user.setType(0);
        user.setStatus(0);
        user.setHeaderUrl("https://www.nowcode.com/103.png");
        user.setCreateTime(new Date());
        int lines = userMapper.insertUser(user);
        System.out.println(lines);
        System.out.println(user.getId());
    }

    @Test
    void testUpdate() {
        userMapper.updateStatus(150,1);
        userMapper.updatePassword(150,"123youc4");
        userMapper.updateHeaderUrl(150,"http://www.nowcode.com/500.png");
    }

    @Test
    void TestSelectPosts() {
        List<DiscussPost> discussPosts = discussPostMapper.selectDiscussPosts(149, 0, 10,0);
        for (DiscussPost discussPost : discussPosts) {
            System.out.println(discussPost);
        }
        System.out.println("------------------");

        int count = discussPostMapper.selectDiscussPostRows(149);
        System.out.println(count);
    }

    @Test
    void TestinsertLoginTicket() {
        LoginTicket loginTicket = new LoginTicket();
        loginTicket.setUserId(101);
        loginTicket.setTicket("abc");
        loginTicket.setStatus(0);
        loginTicket.setExpired(new Date(System.currentTimeMillis()+1000*60*10));

        loginTicketMapper.insertLoginTicket(loginTicket);
    }

    @Test
    void TestSelectLoginTicket() {
        String ticket="abc";
        LoginTicket loginTicket = loginTicketMapper.selectByTicket(ticket);
        System.out.println(loginTicket);

        loginTicketMapper.updateStatus(ticket,1);
        loginTicket=loginTicketMapper.selectByTicket(ticket);
        System.out.println(loginTicket);
    }

    @Test
    void testSelectLetters() {
        List<Message> messages = messageMapper.selectConversations(111, 0, 20);
        for (Message message : messages) {
            System.out.println(message);
        }

        int count = messageMapper.selectConversationCount(111);
        System.out.println(count);

        messages = messageMapper.selectLetters("111_112", 0, 10);
        for (Message message : messages) {
            System.out.println(message);
        }

        count = messageMapper.selectLetterCount("111_112");
        System.out.println(count);

        count = messageMapper.selectLetterUnreadCount(131, "111_131");
        System.out.println(count);
    }
}
