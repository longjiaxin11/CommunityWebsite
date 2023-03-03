package com.ljx.community;


import com.ljx.community.service.DataService;
import com.ljx.community.util.RedisKeyUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.BoundValueOperations;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.test.context.ContextConfiguration;

import java.text.SimpleDateFormat;
import java.util.Date;

@SpringBootTest
@ContextConfiguration(classes = CommunityWebsiteApplication.class)
public class RedisTest {


    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private DataService dataService;

    private SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd");

    @Test
    void testString() {
        String redisKey = "test:count";

        redisTemplate.opsForValue().set(redisKey,1);

        System.out.println(redisTemplate.opsForValue().get(redisKey));
        System.out.println(redisTemplate.opsForValue().increment(redisKey));
        System.out.println(redisTemplate.opsForValue().decrement(redisKey));
    }

    @Test
    void TestHash() {
        String redisKey = "test:user";

        redisTemplate.opsForHash().put(redisKey,"id","1");
        redisTemplate.opsForHash().put(redisKey,"username","张三");

        System.out.println(redisTemplate.opsForHash().get(redisKey,"id"));
        System.out.println(redisTemplate.opsForHash().get(redisKey,"username"));
    }

    @Test
    void TestList() {
        String redisKey = "test:ids";

        redisTemplate.opsForList().leftPush(redisKey,101);
        redisTemplate.opsForList().leftPush(redisKey,102);
        redisTemplate.opsForList().leftPush(redisKey,103);

        System.out.println(redisTemplate.opsForList().size(redisKey));
    }

    /* 多次访问同一个key  */

    @Test
    void testBoundOperations() {
        String redisKey = "test:count";
        BoundValueOperations operations = redisTemplate.boundValueOps(redisKey);
        operations.increment();
        operations.increment();
        operations.increment();
        operations.increment();
        operations.increment();
        System.out.println(operations.get());
    }
    
    /* 编程式事务 */

    @Test
    void testTeansactional() {
        Object obj = redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations operations) throws DataAccessException {
                String redisKey = "test:tx";
                operations.multi();

                operations.opsForSet().add(redisKey,"张三");
                operations.opsForSet().add(redisKey,"李四");
                operations.opsForSet().add(redisKey,"王五");
                System.out.println(redisTemplate.opsForSet().members(redisKey));

                return operations.exec();
            }
        });
        System.out.println(obj);
    }

    /* 测试DAU 方法  */

    @Test
    void testDAU() {
        Date start = new Date();
        Date end = new Date();

        dataService.recordDAU(11);
        dataService.recordDAU(11);
        dataService.recordDAU(12);
        dataService.recordDAU(13);

        String redisKey = RedisKeyUtil.getDAUKey(df.format(new Date()));
        System.out.println(redisTemplate.opsForValue().getBit(redisKey, 13));
        System.out.println(redisTemplate.opsForValue().getBit(redisKey, 10));
        System.out.println(redisTemplate.opsForValue().getBit(redisKey, 100));
        System.out.println(redisTemplate.opsForValue().getBit(redisKey, 11));

        boolean bit = redisTemplate.opsForValue().getBit(redisKey, 11);

        long l = dataService.calculateDAU(start, end);
        System.out.println(l);
    }

    @Test
    void testUV() {
        Date start = new Date();
        Date end = new Date();

        String redisKey = getString();

        redisTemplate.opsForHyperLogLog().add(redisKey,"11");
        redisTemplate.opsForHyperLogLog().add(redisKey,"12");
        redisTemplate.opsForHyperLogLog().add(redisKey,"13");

        Long size = redisTemplate.opsForHyperLogLog().size(redisKey);

        System.out.println(size);
    }

    @Test
    void testUV1() {
        Date start = new Date();
        Date end = new Date();

        redisTemplate.opsForValue().set(getString(),"hello world!");

        System.out.println(redisTemplate.opsForValue().get(getString()));

        //long size = dataService.calculateUV(start, end);

        //System.out.println(size);
    }

    public String getString(){
        String word = "20220102";
        String format = df.format(new Date());
        return RedisKeyUtil.getUVKey(format);
    }
    public void recordUV(String ip) {
        //String redisKey = RedisKeyUtil.getUVKey(df.format(new Date()));
        String format = df.format(new Date());
        String redisKey1 = RedisKeyUtil.getUVKey(format);
        String redisKey2 = "uv:20220302";
        redisTemplate.opsForHyperLogLog().add(redisKey1, ip);
    }
}
