package com.ljx.community;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.annotation.PostConstruct;

@SpringBootApplication
public class CommunityWebsiteApplication {


    @PostConstruct
    public void init(){
        /*  解决Netty冲突问题 */
        /* see  Netty4Utiles.setAvailableProcessors */
        System.setProperty("es.set.netty.runtime.available.processors","false");
    }

    public static void main(String[] args) {
        SpringApplication.run(CommunityWebsiteApplication.class, args);
    }

}
