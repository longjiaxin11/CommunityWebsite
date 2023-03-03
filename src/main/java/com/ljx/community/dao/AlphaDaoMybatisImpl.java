package com.ljx.community.dao;

import org.springframework.stereotype.Repository;

@Repository(value = "mybatis")
public class AlphaDaoMybatisImpl implements AlphaDao{
    @Override
    public String select() {
        return "Mybatis";
    }
}
