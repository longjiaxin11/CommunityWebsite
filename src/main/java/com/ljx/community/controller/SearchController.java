package com.ljx.community.controller;

import com.ljx.community.entity.DiscussPost;
import com.ljx.community.entity.Page;
import com.ljx.community.service.ElasticsearchService;
import com.ljx.community.service.LikeService;
import com.ljx.community.service.UserService;
import com.ljx.community.util.CommunityConstant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class SearchController implements CommunityConstant {

    @Autowired
    private ElasticsearchService elasticsearchService;

    @Autowired
    private UserService userService;

    @Autowired
    private LikeService likeService;


    // search?keyword==xxx
    @RequestMapping(path = "/search",method = RequestMethod.GET)
    public String search(String keyword, Page page, Model model){

        Map<String, Object> discussPostMap =
                elasticsearchService.searchDiscussPost(keyword, page.getCurrentpage() - 1, page.getLimit());

        // 搜索帖子
        List<DiscussPost> searchResult = (List<DiscussPost>) discussPostMap.get("searchResult");

        // 聚合数据
        List<Map<String,Object>> discussPosts = new ArrayList<>();
        if(searchResult!=null){
            for (DiscussPost post : searchResult) {
                Map<String,Object> map = new HashMap();
                /* 帖子 */
                map.put("post",post);
                /* 作者 */
                map.put("user",userService.findUserById(post.getUserId()));
                /* 点赞数量 */
                map.put("likeCount",likeService.findEntityLikeCount(ENTITY_TYPE_POST,post.getId()));

                discussPosts.add(map);
            }
        }
        model.addAttribute("discussPosts",discussPosts);
        model.addAttribute("keyword",keyword);

        // 分页信息
        page.setPath("/search?keyword="+keyword);
        page.setRows(searchResult == null?0: (int) discussPostMap.get("rows"));

        return "/site/search";
    }
}
