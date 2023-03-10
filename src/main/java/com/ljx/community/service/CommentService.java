package com.ljx.community.service;

import com.ljx.community.dao.CommentMapper;
import com.ljx.community.dao.DiscussPostMapper;
import com.ljx.community.entity.Comment;
import com.ljx.community.util.CommunityConstant;
import com.ljx.community.util.SensitiveFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.HtmlUtils;

import javax.swing.text.html.parser.Entity;
import java.util.List;

@Service
public class CommentService implements CommunityConstant {

    @Autowired
    CommentMapper commentMapper;

    @Autowired
    SensitiveFilter sensitiveFilter;

    @Autowired
    DiscussPostService discussPostService;

    public List<Comment> findCommentsByEntity(int entityType,int entityId,int offset,int limit){
        return commentMapper.selectCommentByEntity(entityType,entityId,offset,limit);
    }

    public int findCommentCount(int entityType,int entityId){
        return commentMapper.selectCountByEntity(entityType,entityId);
    }

    @Transactional(isolation = Isolation.READ_COMMITTED,propagation = Propagation.REQUIRED)
    public int addComment(Comment comment){
        if(comment==null){
            throw new IllegalArgumentException("参数不能为空");
        }
        /* 添加评论 */
        comment.setContent(HtmlUtils.htmlEscape(comment.getContent()));
        comment.setContent(sensitiveFilter.filter(comment.getContent()));
        int rows = commentMapper.insertComment(comment);

        /* 更新帖子（discuss_post里有一个comment_count字段）的评论数量 */
        if(comment.getEntityType() == ENTITY_TYPE_POST){
            int count = commentMapper.selectCountByEntity(ENTITY_TYPE_POST, comment.getEntityId());
            discussPostService.updateCommentCount(comment.getEntityId(),count);
        }
        return rows;
    }

    public Comment findCommentById(int id){
        return commentMapper.selectCommentById(id);
    }

    public List<Comment> findCommentsByUser(int entityType,int userId,int offset,int limit){
        return commentMapper.selectCommentByUser(entityType, userId, offset, limit);
    }

    public int findCountByUser(int entityType,int userId){
        return commentMapper.selectCountByUser(entityType,userId);
    }

}
