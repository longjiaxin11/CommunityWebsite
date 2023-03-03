package com.ljx.community.dao;

import com.ljx.community.entity.DiscussPost;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface DiscussPostMapper {
    /* orderMode : 0:默认排，按最新 1：按最热排 */
    List<DiscussPost> selectDiscussPosts(int userId,int offset,int limit,int orderMode);

    /*起别名，动态sql中，如果方法只有一个参数，那么这个参数一定要起别名*/
    int selectDiscussPostRows(@Param(value = "userId") int userId);

    int insertDiscussPost(DiscussPost discussPost);

    DiscussPost selectDiscussPostById(int id);

    int updateCommentCount(int id,int commentCount);

    int updateType(int id,int type);

    int updateStatus(int id,int status);
    int updateScore(int id,double score);
}
