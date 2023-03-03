package com.ljx.community.service;

import com.fasterxml.jackson.databind.node.POJONode;
import com.ljx.community.dao.AlphaDao;
import com.ljx.community.dao.DiscussPostMapper;
import com.ljx.community.dao.UserMapper;
import com.ljx.community.entity.DiscussPost;
import com.ljx.community.entity.User;
import com.ljx.community.util.CommunityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.Date;
import java.util.Random;


@Service("AlphaService")
public class AlphaService {

    @Autowired()
    private AlphaDao alphaDao;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private DiscussPostMapper discussPostMapper;

    @Autowired
    TransactionTemplate transactionTemplate;

//
//    public AlphaService(){
//        System.out.println("实例化AlphaService");
//    }
//    @PostConstruct()
//    public void init(){
//        System.out.println("初始化AlphaService");
//    }
//    @PreDestroy()
//    public void destroy(){
//        System.out.println("销毁AlphaService");
//    }

    public String find(){
        return alphaDao.select();
    }


    /*
    *Propagation.REQUIRED ：支持当前事务（外部事务）,如果没有事务调用我，则创建新事物
     *Propagation.REQUIRES_NEW ： 创建一个新事务，并且暂停当前事务（外部事务）
    *Propagation.NESTED：如果当前存在事务（外部事务），则嵌套在该事物里执行（自己有独立的提交和回滚），否则和REQUIRED一样
    *
    * */
    @Transactional(isolation = Isolation.READ_COMMITTED,propagation = Propagation.REQUIRED)
    public Object save1(){
        /* 新增用户*/
        User user=new User();
        user.setUsername("alpha");
        user.setSalt(CommunityUtil.generateUUID().substring(0,5));
        user.setPassword(CommunityUtil.md5("123"+user.getSalt()));
        user.setEmail("alpha@qq.com");
        user.setType(0);
        user.setStatus(0);
        user.setActivationCode(CommunityUtil.generateUUID());
        //随机的0~1000中的数字
        user.setHeaderUrl("http://images.nowcoder.com/head/99t.png");
        user.setCreateTime(new Date());
        userMapper.insertUser(user);

        /* 新增帖子 */
        DiscussPost post=new DiscussPost();
        post.setUserId(user.getId());
        post.setTitle("hello");
        post.setContent("型人报道");
        post.setCreateTime(new Date());
        discussPostMapper.insertDiscussPost(post);

        int num=1/0;
        return "ok";
    }

    public Object save2(){
        transactionTemplate.setIsolationLevel(TransactionDefinition.ISOLATION_READ_COMMITTED);
        transactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        return transactionTemplate.execute(new TransactionCallback<Object>() {
            @Override
            public Object doInTransaction(TransactionStatus status) {
                /* 新增用户*/
                User user=new User();
                user.setUsername("alpha");
                user.setSalt(CommunityUtil.generateUUID().substring(0,5));
                user.setPassword(CommunityUtil.md5("123"+user.getSalt()));
                user.setEmail("alpha@qq.com");
                user.setType(0);
                user.setStatus(0);
                user.setActivationCode(CommunityUtil.generateUUID());
                //随机的0~1000中的数字
                user.setHeaderUrl("http://images.nowcoder.com/head/99t.png");
                user.setCreateTime(new Date());
                userMapper.insertUser(user);

                /* 新增帖子 */
                DiscussPost post=new DiscussPost();
                post.setUserId(user.getId());
                post.setTitle("hello");
                post.setContent("型人报道");
                post.setCreateTime(new Date());
                discussPostMapper.insertDiscussPost(post);

                int num=1/0;
                return "ok";
            }
        });
    }


}
