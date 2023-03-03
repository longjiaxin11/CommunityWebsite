package com.ljx.community.controller;

import com.ljx.community.annotation.LoginRequired;
import com.ljx.community.entity.Comment;
import com.ljx.community.entity.DiscussPost;
import com.ljx.community.entity.Page;
import com.ljx.community.entity.User;
import com.ljx.community.service.*;
import com.ljx.community.util.CommunityConstant;
import com.ljx.community.util.CommunityUtil;
import com.ljx.community.util.HostHolder;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/user")
public class UserController implements CommunityConstant {

    private Logger logger = LoggerFactory.getLogger(UserController.class);
    @Value("${community.path.upload}")
    private String uploadPath;

    @Value("${community.path.domain}")
    private String domain;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Autowired
    private UserService userService;

    @Autowired
    private LikeService likeService;

    @Autowired
    private FollowService followService;

    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private CommentService commentService;

    @Autowired
    private HostHolder hostHolder;


    @LoginRequired
    @RequestMapping(path = "/setting",method = RequestMethod.GET )
    public String getSettingPage(){
        return "/site/setting";
    }

    @LoginRequired
    @RequestMapping(path = "/upload",method = RequestMethod.POST)
    public String uploadHeader(MultipartFile headerImage, Model model){
        if(headerImage==null){
            model.addAttribute("error","您还没有选择图片");
            return "/site/setting";
        }

        String fileName = headerImage.getOriginalFilename();
        String suffix = fileName.substring(fileName.lastIndexOf('.') );
        if(StringUtils.isBlank(suffix)){
            model.addAttribute("error","文件格式不正确");
            return "/site/setting";
        }

        /* 生成随机文件名 ,不能使用用户原本的文件名，可能会相同产生冲突*/
        fileName = CommunityUtil.generateUUID()+suffix;
        /*  确定文件存放的路径  */
        File dest = new File(uploadPath+"/"+fileName);
        try {
            /*存储文件*/
            headerImage.transferTo(dest);
        } catch (IOException e) {
            logger.error("上传文件失败："+e.getMessage());
            throw new RuntimeException("上传文件失败，服务器发生异常"+e);
        }

        /*更新当前用户的头像的路径，web下的*/
        /* http://localhost:8080/community/user/header/xxx.png  */
        User user=hostHolder.getUser();
        String headerUrl = domain + contextPath +"/user/header/" + fileName;
        userService.updateHeader(user.getId(),headerUrl);

        return "redirect:/index";
    }



    @RequestMapping(path = "/header/{fileName}",method = RequestMethod.GET)
    public void getHeader(@PathVariable("fileName") String fileName, HttpServletResponse response){
        /* 服务器存放路径 */
        fileName=uploadPath+"/"+fileName;
        /* 文件后缀 */
        String suffix = fileName.substring(fileName.lastIndexOf(".")+1);
        /* 响应图片 */
        response.setContentType("image/"+suffix);
        try(
                FileInputStream fis = new FileInputStream(fileName);
        ) {
            OutputStream os=response.getOutputStream();
            byte[] buffer = new byte[1024];
            int b= 0;
            while((b = fis.read(buffer))!=-1){
                os.write(buffer,0,b);
            }
        } catch (IOException e) {
            logger.error("读取头像失败:"+e.getMessage());
        }
    }

    /* 修改密码 */
    @LoginRequired
    @RequestMapping(path = "/update",method = RequestMethod.POST)
    public String updatePassword(Model model, String oldPassword,String newPassword,String newPasswordRepeat){
        User user=hostHolder.getUser();
        Map<String, Object> map = userService.updatePassword(user, oldPassword, newPassword,newPasswordRepeat);

        /* 修改失败 */
        if(!map.containsKey("updateMsg")){
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                model.addAttribute(entry.getKey(),entry.getValue());
            }
            return "/site/setting";
        }

        model.addAttribute("msg",map.get("updateMsg"));
        model.addAttribute("target","/index");
        return "/site/operate-result";
    }

    //个人主页
    @RequestMapping(path = "/profile/{userId}",method = RequestMethod.GET)
    public String getProfilePage(@PathVariable("userId") int userId,Model model){
        User user=userService.findUserById(userId);
        if(user==null){
            throw new RuntimeException("该用户不存在");
        }

        // 用户
        model.addAttribute("user",user);
        /* 点赞数量 */
        int likeCount = likeService.findUserLikeCount(userId);
        model.addAttribute("likeCount",likeCount);

        /* 查询关注数量 */
        long followeeCount = followService.findFolloweeCount(userId, ENTITY_TYPE_USER);
        model.addAttribute("followeeCount",followeeCount);

        /* 查询粉丝数量 */
        long followerCount = followService.findFollowerCount(ENTITY_TYPE_USER, userId);
        model.addAttribute("followerCount",followerCount);


        /* 当前用户是否已关注该用户 */
        boolean hasFollowed = false;
        if(hostHolder.getUser() != null){
            hasFollowed = followService.hasFollowed(hostHolder.getUser().getId(),ENTITY_TYPE_USER,userId);
        }
        model.addAttribute("hasFollowed",hasFollowed);

        /* 当前用户是否是这个个人主页本人 */
        boolean isMe=false;
        if(hostHolder.getUser() != null){
            isMe = hostHolder.getUser().getId() == userId;
        }
        model.addAttribute("isMe",isMe);

        return "/site/profile";
    }

    /* 我的帖子 */
    @RequestMapping(path = "/post/{userId}",method = RequestMethod.GET)
    public String getPostPage(@PathVariable("userId") int userId, Page page, Model model){
        User user=userService.findUserById(userId);
        if(user==null){
            throw new RuntimeException("该用户不存在");
        }

        model.addAttribute("user",user);

        int count = discussPostService.findDiscussPostRows(userId);

        page.setRows(count);
        page.setLimit(5);
        page.setPath("/user/post/"+userId);

        /* 帖子默认按最新排 */
        List<DiscussPost> discussPostList = discussPostService.findDiscussPosts(userId, page.getOffset(), page.getLimit(),0);
        List<Map<String,Object>> discussPostVo = new ArrayList<>();

        if(discussPostList!=null){
            for (DiscussPost post : discussPostList) {
                Map<String,Object> map = new HashMap<>();
                map.put("title",post.getTitle());
                map.put("content",post.getContent());
                map.put("createTime",post.getCreateTime());
                map.put("likeCount",likeService.findEntityLikeCount(ENTITY_TYPE_POST,post.getId()));
                map.put("discussPostId",post.getId());

                discussPostVo.add(map);
            }
        }
        model.addAttribute("discussPosts",discussPostVo);
        model.addAttribute("discussPostCount",count);

        /* 当前用户是否是这个个人主页本人 */
        boolean isMe=false;
        if(hostHolder.getUser() != null){
            isMe = hostHolder.getUser().getId() == userId;
        }
        model.addAttribute("isMe",isMe);

        return "/site/my-post";
    }


    /* 我的回复 */
    @RequestMapping(path = "/reply/{userId}",method = RequestMethod.GET)
    public String getReplyPage(@PathVariable("userId") int userId, Page page, Model model){
        User user=userService.findUserById(userId);
        if(user==null){
            throw new RuntimeException("该用户不存在");
        }

        /* 当前用户是否是这个个人主页本人 */
        boolean isMe=false;
        if(hostHolder.getUser() != null){
            isMe = hostHolder.getUser().getId() == userId;
        }
        if(!isMe){
            return "redirect:/index";
        }

        model.addAttribute("isMe",isMe);


        model.addAttribute("user",user);

        int count = commentService.findCountByUser(CommunityConstant.ENTITY_TYPE_POST,userId);

        page.setRows(count);
        page.setLimit(5);
        page.setPath("/user/reply/"+userId);

        model.addAttribute("replyCount",count);

        List<Comment> commentList = commentService.findCommentsByUser(CommunityConstant.ENTITY_TYPE_POST, userId, page.getOffset(), page.getLimit());
        List<Map<String,Object>> commentListVo = new ArrayList<>();

        for (Comment comment : commentList) {
            Map<String,Object> map = new HashMap<>();
            DiscussPost post = discussPostService.findDiscussPostById(comment.getEntityId());
            map.put("title",post.getTitle());
            map.put("discussPostId",post.getId());
            map.put("content",comment.getContent());
            map.put("createTime",comment.getCreateTime());

            commentListVo.add(map);
        }

        model.addAttribute("comments",commentListVo);


        return "/site/my-reply";
    }

}
