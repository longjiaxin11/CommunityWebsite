package com.ljx.community.util;

import com.ljx.community.entity.User;
import org.springframework.stereotype.Component;


/*
* 持有用户的信息，多线程环境实现，代替session对象
* */
@Component
public class HostHolder {

    /* 以线程为key，所以get方法里没有参数，参数是thread.currentThread */
    private ThreadLocal<User> users=new ThreadLocal<>();

    public void setUser(User user){
        users.set(user);
    }

    public User getUser(){
        return users.get();
    }

    public void clear(){
        users.remove();
    }

}
