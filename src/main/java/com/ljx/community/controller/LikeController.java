package com.ljx.community.controller;

import com.ljx.community.annotation.LoginRequired;
import com.ljx.community.entity.Event;
import com.ljx.community.entity.User;
import com.ljx.community.event.EventProducer;
import com.ljx.community.service.LikeService;
import com.ljx.community.util.CommunityConstant;
import com.ljx.community.util.CommunityUtil;
import com.ljx.community.util.HostHolder;
import com.ljx.community.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Map;

@Controller
public class LikeController implements CommunityConstant {

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private LikeService likeService;

    @Autowired
    private EventProducer eventProducer;

    @Autowired
    private RedisTemplate redisTemplate;

    @LoginRequired
    @RequestMapping(path = "/like",method = RequestMethod.POST)
    @ResponseBody
    public String like(int entityType,int entityId,int entityUserId,int postId){
        User user = hostHolder.getUser();

        //实现点赞
        likeService.like(user.getId(),entityType,entityId,entityUserId);
        //数量
        long likecount = likeService.findEntityLikeCount(entityType,entityId);
        /* 状态 */
        int likeStatus = likeService.findEntityLikeStatus(user.getId(), entityType, entityId);

        /* result */
        Map<String,Object> map = new HashMap<>();
        map.put("likeCount",likecount);
        map.put("likeStatus",likeStatus);

        /* 触发点赞事件，取消点赞就不触发 */
        if(likeStatus == 1){
            Event event=new Event()
                    .setTopic(TOPIC_LIKE)
                    .setUserId(hostHolder.getUser().getId())
                    .setEntityType(entityType)
                    .setEntityId(entityId)
                    .setEntityUserId(entityUserId)
                    .setData("postId",postId);
            eventProducer.fireEvent(event);
        }

        if(entityType == ENTITY_TYPE_POST){
            /* 计算帖子分数 */
            String redisKey = RedisKeyUtil.getPostScoreKey();
            redisTemplate.opsForSet().add(redisKey,postId);
        }


        return CommunityUtil.getJSONString(0,null,map);
    }

}
