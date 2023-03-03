package com.ljx.community.controller;

import com.google.code.kaptcha.Producer;
import com.ljx.community.entity.User;
import com.ljx.community.service.UserService;
import com.ljx.community.util.CommunityConstant;
import com.ljx.community.util.CommunityUtil;
import com.ljx.community.util.RedisKeyUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.imageio.ImageIO;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Controller
public class LoginController implements CommunityConstant {

    @Autowired
    UserService userService;

    @Autowired
    private Producer kaptchaProducer;

    @Autowired
    private RedisTemplate redisTemplate;

    @Value("${server.servlet.context-path}")
    private String contextPath;


    private static final Logger logger= LoggerFactory.getLogger(LoginController.class);

    @RequestMapping(path = "/login",method = RequestMethod.GET)
    public String getLoginPage(){
        return "/site/login";
    }

    @RequestMapping(path = "/register",method = RequestMethod.GET)
    public String getRegisterPage(){
        return "/site/register";
    }

    /* 注册表单提交 */
    @RequestMapping(path="/register",method = RequestMethod.POST)
    public String register(Model model, User user){
        Map<String, Object> map = userService.register(user);

        /*注册成功，后跳到主页，进行激活功能*/
        if(map==null || map.isEmpty()){
            //System.out.println("111111");
            model.addAttribute("msg","注册成功，我们已经向您的邮箱发送了一封激活邮件，请尽快激活");
            model.addAttribute("target","/index");
            return "/site/operate-result";
        }else {
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                model.addAttribute(entry.getKey(),entry.getValue());
            }
            //System.out.println("22222222");
            return "/site/register";
        }
    }

    /* 激活邮箱里的验证码 */
    @RequestMapping(path = "/activation/{userId}/{code}",method = RequestMethod.GET)
    public String activation(Model model, @PathVariable("userId") int userId,@PathVariable("code") String code){
        int result = userService.activation(userId,code);
        if(result == ACTIVATION_SUCCESS){
            model.addAttribute("msg","注册成功，即将进入登录页面");
            model.addAttribute("target","/login");
        } else if (result == ACTIVATION_REPEAT) {
            model.addAttribute("msg","重复注册，即将返回主页");
            model.addAttribute("target","/index");
        }else{
            model.addAttribute("msg","激活失败，您的激活码错误");
            model.addAttribute("target","/index");
        }
        return "/site/operate-result";
    }

    @RequestMapping(path = "/kaptcha",method = RequestMethod.GET)
    public void getKaptcha(HttpServletResponse response/*, HttpSession session*/){
        /*生成验证码*/
        String text = kaptchaProducer.createText();
        BufferedImage image = kaptchaProducer.createImage(text);

        /* 将验证码传入session */
        //session.setAttribute("kaptcha",text);

        // 验证码的归属
        String kaptchaOwner = CommunityUtil.generateUUID();
        Cookie cookie = new Cookie("kaptchaOwner",kaptchaOwner);
        cookie.setMaxAge(60);
        cookie.setPath(contextPath);
        response.addCookie(cookie);
        // 将验证码存入redis
        String redisKey = RedisKeyUtil.getKaptchaKey(kaptchaOwner);
        redisTemplate.opsForValue().set(redisKey,text,60, TimeUnit.SECONDS);



        /* 将图片输出给浏览器 */
        response.setContentType("image/png");
        try {
            ServletOutputStream os = response.getOutputStream();
            ImageIO.write(image,"png",os);
        } catch (IOException e) {
            logger.error("响应验证码失败"+e.getMessage());
        }
    }

    /* 如果请求里的参数是一个对象，会放入到model里去，如果是string这种则不会，可以手动放入model，或者request的parm里取  */
    @RequestMapping(path = "/login",method = RequestMethod.POST)
    public String login(String username,String password,String code,boolean rememberme,Model model,
                        /*HttpSession session,*/HttpServletResponse response,
                        @CookieValue(value = "kaptchaOwner",required = false) String kaptchaOwner){

        /* 检查验证码 */
        //String kaptcha = (String) session.getAttribute("kaptcha");
        String kaptcha = null;
        if(StringUtils.isNotBlank(kaptchaOwner)){
            String redisKey = RedisKeyUtil.getKaptchaKey(kaptchaOwner);
            kaptcha = (String)redisTemplate.opsForValue().get(redisKey);
        }

        if(StringUtils.isBlank(kaptcha) || StringUtils.isBlank(code) || !kaptcha.equalsIgnoreCase(code)){
            model.addAttribute("codeMsg","验证码不正确");
            return "/site/login";
        }

        /* 检查账号，密码 */
        int expiredSeconds = rememberme ? REMEMBER_EXPIRED_SECONDS : DEFAULT_EXPIRED_SECONDS;
        Map<String, Object> map = userService.login(username, password, expiredSeconds);
        if(map.containsKey("ticket")){
            Cookie cookie=new Cookie("ticket",map.get("ticket").toString());
            cookie.setPath(contextPath);
            cookie.setMaxAge(expiredSeconds);
            response.addCookie(cookie);
            return "redirect:/index";
        }else {
            model.addAttribute("usernameMsg",map.get("usernameMsg"));
            model.addAttribute("passwordMsg",map.get("passwordMsg"));
            return "/site/login";
        }
    }

    @RequestMapping(path = "/logout",method = RequestMethod.GET)
    public String logout(@CookieValue(value = "ticket",required = false) String ticket){
        userService.logout(ticket);
        SecurityContextHolder.clearContext();
        return "redirect:/login";
    }

    @RequestMapping(path = "/forget",method = RequestMethod.GET)
    public String forget(){
        return "/site/forget";
    }

    @RequestMapping(path = "/sendForgetMail",method = RequestMethod.POST)
    @ResponseBody
    public String sendForgetMail(String email){

        /* 该邮箱存在，出发发邮件事件 */
        Map<String, Object> map = userService.sendForgetEmail(email);
        if(map.containsKey("emailMsg")){
            return CommunityUtil.getJSONString(403, (String) map.get("emailMsg"));
        }

        return CommunityUtil.getJSONString(0,"验证码已发送");
    }

    @RequestMapping(path = "/forgetPassword",method = RequestMethod.POST)
    public String forgetPassword(String email,String kaptcha,String newPassword,Model model){

        Map<String, Object> map = userService.updateForgetPassword(email, kaptcha, newPassword);

        /*注册成功，后跳到主页，进行激活功能*/
        if(map==null || map.isEmpty()){
            //System.out.println("111111");
            model.addAttribute("msg","找回密码成功，快使用新密码登录吧！");
            model.addAttribute("target","/login");
            return "/site/operate-result";
        }else {
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                model.addAttribute(entry.getKey(),entry.getValue());
            }
            //System.out.println("22222222");
            return "/site/forget";
        }
    }
}
