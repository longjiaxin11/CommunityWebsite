package com.ljx.community.config;

import com.ljx.community.quartz.AlphaJob;
import com.ljx.community.quartz.PostScoreRefreshJob;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.quartz.JobDetailFactoryBean;
import org.springframework.scheduling.quartz.SimpleTriggerFactoryBean;


/* 配置 -> 数据库 -> 调用  只有第一次运行时才把这个配置初始化到数据库中，以后去数据库的表里读配置 */
@Configuration
public class QuartzConfig {

    // FactoryBean 可简化Bean的实例化过程
    // 1 Spring通过FactoryBean封装Bean的实例化过程
    // 2 将FactoryBean装配到Spring容器中
    // 3 将FactoryBean注入给其他的Bean
    // 4 该Bean得到的是这个FactoryBean管理的对象的实例


    /* 配置JobDetail */
    //@Bean
    public JobDetailFactoryBean alphaJobDetail(){
        JobDetailFactoryBean factoryBean = new JobDetailFactoryBean();
        factoryBean.setJobClass(AlphaJob.class);
        factoryBean.setBeanName("alphaJob");
        factoryBean.setGroup("alphaJobGroup");
        factoryBean.setDurability(true);
        factoryBean.setRequestsRecovery(true);
        return factoryBean;
    }


    /* 配置Trigger(SimpleTriggerFactoryBean(简单的，延迟多少时间，频率多少),CronTriggerFactoryBean（复杂的，比方说每个月月底半夜两点做什么事情）) */
    //@Bean
    public SimpleTriggerFactoryBean alphaTrigger(JobDetail alphaJobDetail){
        SimpleTriggerFactoryBean factoryBean = new SimpleTriggerFactoryBean();
        factoryBean.setJobDetail(alphaJobDetail);
        factoryBean.setBeanName("alphaTrigger");
        factoryBean.setGroup("alphaTriggerGroup");
        factoryBean.setRepeatInterval(3000);
        factoryBean.setJobDataMap(new JobDataMap());
        return factoryBean;
    }

    /* 刷新帖子分数任务 */
    @Bean
    public JobDetailFactoryBean postScoreRefreshJobDetail(){
        JobDetailFactoryBean factoryBean = new JobDetailFactoryBean();
        factoryBean.setJobClass(PostScoreRefreshJob.class);
        factoryBean.setBeanName("postScoreRefreshJob");
        factoryBean.setGroup("communityJobGroup");
        factoryBean.setDurability(true);
        factoryBean.setRequestsRecovery(true);
        return factoryBean;
    }


    /* 配置Trigger(SimpleTriggerFactoryBean(简单的，延迟多少时间，频率多少),CronTriggerFactoryBean（复杂的，比方说每个月月底半夜两点做什么事情）) */
    @Bean
    public SimpleTriggerFactoryBean postScoreRefreshTrigger(JobDetail postScoreRefreshJobDetail){
        SimpleTriggerFactoryBean factoryBean = new SimpleTriggerFactoryBean();
        factoryBean.setJobDetail(postScoreRefreshJobDetail);
        factoryBean.setBeanName("postScoreRefreshTrigger");
        factoryBean.setGroup("communityJobGroup");
        factoryBean.setRepeatInterval(1000 * 60 * 5);
        factoryBean.setJobDataMap(new JobDataMap());
        return factoryBean;
    }
}
