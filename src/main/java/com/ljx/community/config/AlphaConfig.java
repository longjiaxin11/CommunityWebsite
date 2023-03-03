package com.ljx.community.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.text.SimpleDateFormat;

@Configuration  //@Configuration这个注解用于写配置类，引入第三方类，用@Bean注解注入
public class AlphaConfig {

    /*Bean组件，方法名就是Bean的名字*/
    @Bean
    public SimpleDateFormat simpleDateFormat(){
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    }
}
