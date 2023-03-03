package com.ljx.community;

import org.junit.jupiter.api.Test;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

@SpringBootTest
@ContextConfiguration(classes = CommunityWebsiteApplication.class)
public class QuartzTests {

    @Autowired
    private Scheduler scheduler;

    @Test
    void testDeleteJob() {
        try {
            boolean result = scheduler.deleteJob(new JobKey("alphaJobDetail","alphaJobGroup"));
            System.out.println(result);
        } catch (SchedulerException e) {
            throw new RuntimeException(e);
        }
    }
}
