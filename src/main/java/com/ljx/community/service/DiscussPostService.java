package com.ljx.community.service;

import com.github.benmanes.caffeine.cache.CacheLoader;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.ljx.community.dao.DiscussPostMapper;
import com.ljx.community.entity.DiscussPost;
import com.ljx.community.util.SensitiveFilter;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;
import org.unbescape.html.HtmlEscape;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class DiscussPostService {

    private static final Logger logger = LoggerFactory.getLogger(DiscussPostService.class);

    @Autowired
    DiscussPostMapper discussPostMapper;

    @Autowired
    SensitiveFilter sensitiveFilter;

    @Value("${caffeine.posts.max-size}")
    private int maxSize;

    @Value("${caffeine.posts.expire-seconds}")
    private int expiredSeconds;

    /* Caffeine核心接口 : Cache , LoadingCache , AsyncLoadingCache */

    /* 帖子列表缓存 */
    private LoadingCache<String , List<DiscussPost>> postListCache;

    /* 帖子总数缓存 */
    private LoadingCache<Integer,Integer> postRowsCache;

    @PostConstruct
    public void init(){
        /* 初始化帖子列表缓存 */
        postListCache = Caffeine.newBuilder()
                .maximumSize(maxSize)
                .expireAfterWrite(expiredSeconds, TimeUnit.SECONDS)
                .build(new CacheLoader<String, List<DiscussPost>>() {
                    @Override
                    public @Nullable List<DiscussPost> load(String key) throws Exception {
                        if (key == null || key.length() == 0){
                            throw new IllegalArgumentException("参数错误！");
                        }

                        String[] params = key.split(":");
                        if (params == null || params.length != 2){
                            throw new IllegalArgumentException("参数错误！");
                        }

                        int offset = new Integer(params[0]);
                        int limit = new Integer(params[1]);

                        /* 二级缓存 -> Redis -> Mysql */
                        logger.debug("load post list from db.");
                        return discussPostMapper.selectDiscussPosts(0,offset,limit,1);
                    }
                });
        /* 初始化帖子总数缓存 */
        postRowsCache = Caffeine.newBuilder()
                .maximumSize(maxSize)
                .expireAfterWrite(expiredSeconds,TimeUnit.SECONDS)
                .build(new CacheLoader<Integer, Integer>() {
                    @Override
                    public @Nullable Integer load(Integer key) throws Exception {

                        logger.debug("load post rows from db.");
                        return discussPostMapper.selectDiscussPostRows(key);
                    }
                });
    }

    public List<DiscussPost> findDiscussPosts(int userId,int offset,int limit,int orderMode){
        /* userId=0 代表访问首页 */
        if(userId == 0 && orderMode==1 ){
            return postListCache.get(offset+":"+limit);
        }

        logger.debug("load post list from db.");
        return discussPostMapper.selectDiscussPosts(userId,offset,limit,orderMode);
    }

    public int findDiscussPostRows(int userId){
         if (userId == 0){
             return postRowsCache.get(userId);
         }

        logger.debug("load post rows from db.");
        return discussPostMapper.selectDiscussPostRows(userId);
    }

    public int addDiscussPost(DiscussPost post){
        if (post==null){
            throw new IllegalArgumentException("参数不能为空");
        }

        /* 转义html标记，防止html注入，类似sql注入那样拼接，导致我的html提前终止 */
        post.setTitle(HtmlUtils.htmlEscape(post.getTitle()));
        post.setContent(HtmlUtils.htmlEscape(post.getContent()));

        /* 过滤敏感词 */
        post.setTitle(sensitiveFilter.filter(post.getTitle()));
        post.setContent(sensitiveFilter.filter(post.getContent()));

        return discussPostMapper.insertDiscussPost(post);
    }

    public DiscussPost findDiscussPostById(int id){
        return discussPostMapper.selectDiscussPostById(id);
    }

    public int updateCommentCount(int id,int commentCount){
        return discussPostMapper.updateCommentCount(id,commentCount);
    }
    public int updateType(int id,int type){
        return discussPostMapper.updateType(id, type);
    }

    public int updateStatus(int id,int status){
        return discussPostMapper.updateStatus(id, status);
    }

    public int updateScore(int id,double score){
        return discussPostMapper.updateScore(id, score);
    }
}
