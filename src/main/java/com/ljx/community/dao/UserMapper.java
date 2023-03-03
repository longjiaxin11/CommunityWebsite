package com.ljx.community.dao;

import com.ljx.community.entity.User;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface UserMapper {

    public User selectById(int id);

    public User selectByName(String username);

    public User selectByEmail(String email);

    public int insertUser(User user);

    public int updateStatus(int id,int status);

    public int updateHeaderUrl(int id,String headerUrl);

    public int updatePassword(int id,String password);

}
