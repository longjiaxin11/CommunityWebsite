package com.ljx.community.dao;

import com.ljx.community.entity.Comment;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface CommentMapper {

    /* entityType 即目标是帖子还是评论，entityId是目标的号，
    根据目标查回复（ 即一个帖子的id是postId，则方法代表查询id为postId的全部回帖）  */
    List<Comment> selectCommentByEntity(int entityType,int entityId,int offset,int limit);

    int selectCountByEntity(int entityType,int entityId);

    int insertComment(Comment comment);

    Comment selectCommentById(int id);

    List<Comment> selectCommentByUser(int entityType,int userId,int offset,int limit);

    int selectCountByUser(int entityType,int userId);
}
