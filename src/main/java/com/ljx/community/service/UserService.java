package com.ljx.community.service;

import com.ljx.community.dao.LoginTicketMapper;
import com.ljx.community.dao.UserMapper;
import com.ljx.community.entity.ForgetTicket;
import com.ljx.community.entity.LoginTicket;
import com.ljx.community.entity.User;
import com.ljx.community.util.CommunityConstant;
import com.ljx.community.util.CommunityUtil;
import com.ljx.community.util.MailClient;
import com.ljx.community.util.RedisKeyUtil;
import javafx.beans.binding.ObjectExpression;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
public class UserService implements CommunityConstant {
    @Autowired
    UserMapper userMapper;

    @Autowired
    MailClient mailClient;

    @Autowired
    TemplateEngine templateEngine;

//    @Autowired
//    LoginTicketMapper loginTicketMapper;

    @Autowired
    RedisTemplate redisTemplate;

    @Value("${community.path.domain}")
    private String domain;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    public User findUserById(int id){
        //return userMapper.selectById(id);

        User user = getCache(id);
        if(user == null){
            user = initCache(id);
        }

        return user;
    }

    /* 重构类似byId */
    public User findUserByEmail(String email){
        //return userMapper.selectByEmail(email);
        User user = getCache(email);
        if(user == null ){
            user = initCache(email);
        }

        return user;
    }

    public Map<String, Object> register(User user){
        Map<String,Object> map=new HashMap<>();
        //检查user是否为null，username是否为空，password是否为空，email是否为空
        if(user==null){
            throw new IllegalArgumentException("参数不能为空!");
        }
        if(StringUtils.isBlank(user.getUsername())){
            map.put("usernameMsg","用户名不能为空");
            return map;
        }
        if(StringUtils.isBlank(user.getPassword())){
            map.put("passwordMsg","密码不能为空");
            return map;
        }
        if(StringUtils.isBlank(user.getEmail())){
            map.put("emailMsg","邮箱不能为空");
            return map;
        }
        //2.检查用户名是否被使用过，email是否被使用过，通过DB来查
        User u=userMapper.selectByName(user.getUsername());
        if(u!=null){
            map.put("usernameMsg","用户名已经存在!");
            return map;
        }

        u=userMapper.selectByEmail(user.getEmail());
        if(u!=null){
            map.put("emailMsg","邮箱已经存在!");
            return map;
        }

        /*  注册用户  */
        user.setSalt(CommunityUtil.generateUUID().substring(0,5));
        user.setPassword(CommunityUtil.md5(user.getPassword()+user.getSalt()));
        user.setType(0);
        user.setStatus(0);
        user.setActivationCode(CommunityUtil.generateUUID());
        //随机的0~1000中的数字
        user.setHeaderUrl(String.format("http://images.nowcoder.com/head/%dt.png", new Random().nextInt(1000)));
        user.setCreateTime(new Date());
        //持久化的同时 在uesr里回填uid
        userMapper.insertUser(user);

        // 激活邮件
        Context context =new Context();
        context.setVariable("email",user.getEmail());
        String url =domain + contextPath + "/activation/" + user.getId() + "/" + user.getActivationCode();
        context.setVariable("url",url);
        String content = templateEngine.process("/mail/activation",context);
        mailClient.sendMail(user.getEmail(), "激活账号",content);

        return map;

    }

    public int activation(int userId,String code){
        User user= userMapper.selectById(userId);
        if(user.getStatus()==1){
            /* 重复激活 */
            return ACTIVATION_REPEAT;
        }else if(user.getActivationCode().equals(code)){
            /* 激活成功 */
            userMapper.updateStatus(userId,1);
            // 修改user时清理缓存
            clearCache(userId);
            return ACTIVATION_SUCCESS;
        }else {
            /* 激活失败 */
            return ACTIVATION_FAILURE;
        }
    }

    public Map<String,Object> login(String username,String password,long expiredSeconds){
        Map<String,Object> map = new HashMap<>();
        /* 1、处理空值 */
        if(StringUtils.isBlank(username)){
            map.put("usernameMsg","用户名不能为空");
            return map;
        }
        if(StringUtils.isBlank(password)){
            map.put("passwordMsg","密码不能为空");
            return map;
        }

        User user = userMapper.selectByName(username);
        /* 验证账号 */
        if(user==null){
            map.put("usernameMsg","账号不存在");
            return map;
        }

        if(user.getStatus() == 0){
            map.put("usernameMsg","该账号未激活");
            return map;
        }

        /* 密码进行比对， */

        if(!user.getPassword().equals(CommunityUtil.md5(password+user.getSalt())) ){
            map.put("passwordMsg","密码不正确");
            return map;
        }

        /* 登录成功，生成登录凭证 */
        LoginTicket loginTicket=new LoginTicket();
        loginTicket.setUserId(user.getId());
        loginTicket.setTicket(CommunityUtil.generateUUID());
        loginTicket.setStatus(0);
        loginTicket.setExpired(new Date(System.currentTimeMillis() + expiredSeconds*1000));

        //loginTicketMapper.insertLoginTicket(loginTicket);
        String redisKey = RedisKeyUtil.getTicketKey(loginTicket.getTicket());
        redisTemplate.opsForValue().set(redisKey,loginTicket);

        map.put("ticket",loginTicket.getTicket());
        return map;

    }

    public void logout(String ticket){
        //loginTicketMapper.updateStatus(ticket,1);
        String redisKey = RedisKeyUtil.getTicketKey(ticket);
        LoginTicket loginTicket = (LoginTicket) redisTemplate.opsForValue().get(redisKey);
        loginTicket.setStatus(1);
        redisTemplate.opsForValue().set(redisKey,loginTicket);
    }

    public LoginTicket findLoginTicket(String ticket){
        //return loginTicketMapper.selectByTicket(ticket);
        String redisKey = RedisKeyUtil.getTicketKey(ticket);
        return (LoginTicket) redisTemplate.opsForValue().get(redisKey);
    }
    public int updateHeader(int userId,String headerUrl){
        //return userMapper.updateHeaderUrl(userId,headerUrl);
        int rows = userMapper.updateHeaderUrl(userId, headerUrl);
        clearCache(userId);

        return rows;
    }
    public Map<String,Object> updatePassword(User user,String oldPassword,String newPassword,String newPasswordRepeat){
        Map<String,Object> map = new HashMap<>();

        /* 新旧密码的判空 */
        if(StringUtils.isBlank(oldPassword)){
            map.put("oldPasswordMsg","原密码不能为空!");
            return map;
        }
        if(StringUtils.isBlank(newPassword)){
            map.put("newPasswordMsg","新密码不能为空!");
            return map;
        }

        if(!newPassword.equals(newPasswordRepeat)){
            map.put("newPasswordMsg","两次密码不一致!");
            return map;
        }


        /* 判断旧密码是否正确 */
        /* 需要加盐后再加密再比较 */
        if(!user.getPassword().equals(CommunityUtil.md5(oldPassword+user.getSalt()))){
            map.put("oldPasswordMsg","原密码不正确!");
            return map;
        }

        userMapper.updatePassword(user.getId(),CommunityUtil.md5(newPassword+user.getSalt()));
        clearCache(user.getId());
        map.put("updateMsg","修改成功!");
        return map;
    }

    public Map<String,Object> sendForgetEmail(String email){
        Map<String,Object> map = new HashMap<>();

        if(StringUtils.isBlank(email)){
            map.put("emailMsg","请填写邮箱！");
            return map;
        }

        User user = findUserByEmail(email);
        if(user == null){
            map.put("emailMsg","该邮箱未注册！");
            return map;
        }

        // 找回密码的邮件
        Context context =new Context();
        context.setVariable("email",user.getEmail());
        String kaptcha = CommunityUtil.generateUUID().substring(0,6);
        context.setVariable("kaptcha",kaptcha);
        String content = templateEngine.process("/mail/forget",context);
        mailClient.sendMail(user.getEmail(), "找回密码",content);

        String redisKey = RedisKeyUtil.getForgetEmailKey(email);
        ForgetTicket forgetTicket = new ForgetTicket().setEmail(email).setKaptcha(kaptcha);
        redisTemplate.opsForValue().set(redisKey,forgetTicket,60*5,TimeUnit.SECONDS);

        return map;
    }
    public Map<String,Object> updateForgetPassword(String email,String kaptcha,String newPassword){
        Map<String,Object> map = new HashMap<>();


        if(StringUtils.isBlank(email)){
            map.put("emailMsg","邮箱不能为空!");
            return map;
        }

        if(StringUtils.isBlank(kaptcha)){
            map.put("kaptchaMsg","验证码不能为空");
            return map;
        }

        /* 新旧密码的判空 */
        if(StringUtils.isBlank(newPassword)){
            map.put("newPasswordMsg","新密码不能为空!");
            return map;
        }

        User user = findUserByEmail(email);
        if(user == null){
            map.put("emailMsg","该邮箱未注册!");
            return map;
        }

        String redisKey = RedisKeyUtil.getForgetEmailKey(email);
        ForgetTicket forgetTicket = (ForgetTicket) redisTemplate.opsForValue().get(redisKey);

        if (forgetTicket==null){
            map.put("kaptchaMsg","验证码已过期!");
            return map;
        }

        if(!(email.equals(forgetTicket.getEmail()) && kaptcha.equals(forgetTicket.getKaptcha()) ) ){
            map.put("kaptchaMsg","验证码不正确!");
            return map;
        }


        userMapper.updatePassword(user.getId(),CommunityUtil.md5(newPassword+user.getSalt()));
        clearCache(user.getId());
        return map;
    }


    public User findUserByName(String username){
        return userMapper.selectByName(username);
    }

    /* 重构findById
    *  1、优先从缓存中取值
    *  2、取不到时初始化缓存数据
    *  3、当数据发生变更时，清楚缓存数据
    * */

    // 1、优先从缓存中取值
    private User getCache(int userId){
        String redisKey = RedisKeyUtil.getUserKey(userId);
        return (User) redisTemplate.opsForValue().get(redisKey);
    }

    private User getCache(String email){
        String redisKey = RedisKeyUtil.getEmailKey(email);
        return (User) redisTemplate.opsForValue().get(redisKey);
    }



    // 2、取不到时初始化缓存数据
    private User initCache(int userId){
        User user = userMapper.selectById(userId);
        String redisKey = RedisKeyUtil.getUserKey(userId);
        redisTemplate.opsForValue().set(redisKey,user,3600, TimeUnit.SECONDS);
        return user;
    }

    private User initCache(String email){
        User user = userMapper.selectByEmail(email);
        String redisKey = RedisKeyUtil.getEmailKey(email);
        redisTemplate.opsForValue().set(redisKey,user,3600, TimeUnit.SECONDS);
        return user;
    }


    // 3、当数据发生变更时，清楚缓存数据
    private void clearCache(int userId){
        String redisKeyId = RedisKeyUtil.getUserKey(userId);
        String redisKeyEmail = RedisKeyUtil.getEmailKey(findUserById(userId).getEmail());

        redisTemplate.delete(redisKeyEmail);
        redisTemplate.delete(redisKeyId);
    }

    private void clearCache(String email){
        String redisKey = RedisKeyUtil.getEmailKey(email);
        redisTemplate.delete(redisKey);
    }

    /*  */
    public Collection<? extends GrantedAuthority> getAuthorities(int userId){
        User user = userMapper.selectById(userId);
        List<GrantedAuthority> list = new ArrayList<>();
        list.add(new GrantedAuthority() {
            @Override
            public String getAuthority() {
                switch (user.getType()){
                    case 1:
                        return AUTHORITY_ADMIN;
                    case 2:
                        return AUTHORITY_MODERATOR;
                    default:
                        return AUTHORITY_USER;
                }
            }
        });
        return list;
    }
}
