package com.ljx.community.controller;

import com.ljx.community.entity.*;
import com.ljx.community.event.EventProducer;
import com.ljx.community.service.CommentService;
import com.ljx.community.service.DiscussPostService;
import com.ljx.community.service.LikeService;
import com.ljx.community.service.UserService;
import com.ljx.community.util.CommunityConstant;
import com.ljx.community.util.CommunityUtil;
import com.ljx.community.util.HostHolder;
import com.ljx.community.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Controller
@RequestMapping(path = "/discuss")
public class DiscussPostController implements CommunityConstant {

    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private UserService userService;

    @Autowired
    private CommentService commentService;

    @Autowired
    private LikeService likeService;

    @Autowired
    private EventProducer eventProducer;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    HostHolder hostHolder;

    /* 发布帖子 */
    @RequestMapping(path = "/add",method = RequestMethod.POST)
    @ResponseBody
    public String addDiscussPost(String title,String content){
        User user = hostHolder.getUser();
        if(user==null){
            return CommunityUtil.getJSONString(403,"你还没有登录！");
        }

        DiscussPost post =  new DiscussPost();
        post.setUserId(user.getId());
        post.setTitle(title);
        post.setContent(content);
        post.setCreateTime(new Date());
        discussPostService.addDiscussPost(post);

        /* 触发发帖事件，把帖子存到elasticsearch服务器中 */
        Event event = new Event()
                .setTopic(TOPIC_PUBLISH)
                .setUserId(user.getId())
                .setEntityType(ENTITY_TYPE_POST)
                .setEntityId(post.getId());
        eventProducer.fireEvent(event);

        /* 计算帖子分数 */
        String redisKey = RedisKeyUtil.getPostScoreKey();
        redisTemplate.opsForSet().add(redisKey,post.getId());

        /* 如果值之前报错了，统一处理，这里不单独处理 */
        return CommunityUtil.getJSONString(0,"发布成功！");
    }

    /* 帖子的详情页面 */
    @RequestMapping(path = "/detail/{discussPostId}",method = RequestMethod.GET)
    public String getDiscussPost(@PathVariable("discussPostId") int discussPostId, Model model, Page page){
        //帖子
        DiscussPost discussPost = discussPostService.findDiscussPostById(discussPostId);
        model.addAttribute("post",discussPost);

        /* 作者  */
        User user = userService.findUserById(discussPost.getUserId());
        model.addAttribute("user",user);
        /* 点赞 */
        long likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_POST, discussPostId);
        model.addAttribute("likeCount",likeCount);
        /* 点赞状态 */
        int likeStatus = hostHolder.getUser()==null ? 0
                :likeService.findEntityLikeStatus(hostHolder.getUser().getId(),ENTITY_TYPE_POST,discussPostId);
        model.addAttribute("likeStatus",likeStatus);

        /* 查评论的分页信息 */
        page.setLimit(5);
        page.setPath("/discuss/detail/" + discussPostId);
        page.setRows(discussPost.getCommentCount());
        model.addAttribute("page",page);

        /*
         * 评论：给帖子的评论
         * 回复：给评论的评论
         * */


        List<Comment> commentList = commentService.findCommentsByEntity (
                ENTITY_TYPE_POST, discussPost.getId(), page.getOffset(), page.getLimit());
        /* 评论：Vo列表  */
        List<Map<String,Object>> commentVoList = new ArrayList<>();
        if(commentList!=null){
            for (Comment comment : commentList) {
                /* 评论Vo */
                Map<String,Object> commentVo = new HashMap<>();
                /* 帖子的评论 */
                commentVo.put("comment",comment);
                /* 评论的作者信息  */
                commentVo.put("user",userService.findUserById(comment.getUserId()));
                /* 点赞 */
                likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_COMMENT, comment.getId());
                commentVo.put("likeCount",likeCount);
                /* 点赞状态 */
                likeStatus = hostHolder.getUser()==null ? 0
                        :likeService.findEntityLikeStatus(hostHolder.getUser().getId(),ENTITY_TYPE_COMMENT,comment.getId());
                commentVo.put("likeStatus",likeStatus);


                /* 再查评论的回复，  */
                List<Comment> replyList = commentService.findCommentsByEntity(
                        ENTITY_TYPE_COMMENT, comment.getId(), 0, Integer.MAX_VALUE);
                List<Map<String,Object>> replyVoList = new ArrayList<>();
                for (Comment reply : replyList) {
                    /* 回复Vo */
                    Map<String,Object> replyVo = new HashMap<>();
                    replyVo.put("reply",reply);
                    replyVo.put("user",userService.findUserById(reply.getUserId()));
                    /* 回复目标
                    *  先看这个comment是否是回复别人的，即有没有targetId，若没有则为null，如有则带上
                    *   */
                    User target = reply.getTargetId() == 0 ? null : userService.findUserById(reply.getTargetId());
                    replyVo.put("target",target);

                    /* 点赞 */
                    likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_COMMENT, reply.getId());
                    replyVo.put("likeCount",likeCount);
                    /* 点赞状态 */
                    likeStatus = hostHolder.getUser()==null ? 0
                            :likeService.findEntityLikeStatus(hostHolder.getUser().getId(),ENTITY_TYPE_COMMENT,reply.getId());
                    replyVo.put("likeStatus",likeStatus);


                    replyVoList.add(replyVo);
                }
                commentVo.put("replies",replyVoList);

                /* 回复数量 */
                int replyCount = commentService.findCommentCount(ENTITY_TYPE_COMMENT,comment.getId());
                commentVo.put("replyCount",replyCount);
                commentVoList.add(commentVo);
            }
        }

        model.addAttribute("comments",commentVoList);

        return "/site/discuss-detail";
    }

    /* 置顶 */
    @RequestMapping(path = "/top",method = RequestMethod.POST)
    @ResponseBody
    public String setTop(int id){
        discussPostService.updateType(id,1);

        /* 触发发帖事件，把帖子存到elasticsearch服务器中 */
        Event event = new Event()
                .setTopic(TOPIC_PUBLISH)
                .setUserId(hostHolder.getUser().getId())
                .setEntityType(ENTITY_TYPE_POST)
                .setEntityId(id);
        eventProducer.fireEvent(event);

        return CommunityUtil.getJSONString(0);
    }


    /* 加精 */
    @RequestMapping(path = "/wonderful",method = RequestMethod.POST)
    @ResponseBody
    public String setWonderful(int id){
        discussPostService.updateStatus(id,1);

        /* 触发发帖事件，把帖子存到elasticsearch服务器中 */
        Event event = new Event()
                .setTopic(TOPIC_PUBLISH)
                .setUserId(hostHolder.getUser().getId())
                .setEntityType(ENTITY_TYPE_POST)
                .setEntityId(id);
        eventProducer.fireEvent(event);

        /* 计算帖子分数 */
        String redisKey = RedisKeyUtil.getPostScoreKey();
        redisTemplate.opsForSet().add(redisKey,id);

        return CommunityUtil.getJSONString(0);
    }


    /* 删除 */
    @RequestMapping(path = "/delete",method = RequestMethod.POST)
    @ResponseBody
    public String setDelete(int id){
        discussPostService.updateStatus(id,2);

        /* 触发删帖事件，把帖子存到elasticsearch服务器中 */
        Event event = new Event()
                .setTopic(TOPIC_DELETE)
                .setUserId(hostHolder.getUser().getId())
                .setEntityType(ENTITY_TYPE_POST)
                .setEntityId(id);
        eventProducer.fireEvent(event);

        return CommunityUtil.getJSONString(0);
    }
}
