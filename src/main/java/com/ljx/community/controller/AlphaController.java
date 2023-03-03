package com.ljx.community.controller;

import com.ljx.community.service.AlphaService;
import com.ljx.community.util.CommunityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/alpha")
public class AlphaController {

    @Autowired
    private AlphaService alphaService;

    @RequestMapping("/hello")
    @ResponseBody
    public String sayHello(){
        return "hello SpringBoot";
    }

    @ResponseBody
    @RequestMapping("/find")
    public String getDate(){
        return alphaService.find();
    }

    @RequestMapping("/TestServlet")
    public void TestServlet(HttpServletRequest request, HttpServletResponse response){
        response.setContentType("text/html;charset=utf-8");
        try (PrintWriter writer = response.getWriter()) {
            writer.print("<h1>牛客网</h1>");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    //get请求
    // /student?current=1&limit=20
    @RequestMapping(path = "students",method = RequestMethod.GET)
    @ResponseBody
    public String getStudents(
            @RequestParam(name = "current",required = false,defaultValue = "1") int current,
            @RequestParam(name = "limit",required = false,defaultValue = "10") int limit){
        System.out.println(current);
        System.out.println(limit);
        return "some students";
    }


    // student/123
    @RequestMapping(path = "/student/{id}",method = RequestMethod.GET)
    @ResponseBody
    public String getStudent(@PathVariable("id") int id) {
        System.out.println(id);
        return "a students";
    }

    //post请求
    @RequestMapping(path = "/student",method = RequestMethod.POST)
    @ResponseBody
    public String saveStudent(String name,int age){
        System.out.println(name);
        System.out.println(age);
        return "success";
    }

    //响应html数据
    @RequestMapping(path = "/teacher",method = RequestMethod.GET)
    public ModelAndView getTeacher(){
        ModelAndView mav=new ModelAndView();
        mav.addObject("name","张三");
        mav.addObject("age",30);
        mav.setViewName("/demo/view");
        return mav;
    }
    @RequestMapping(path = "/school",method = RequestMethod.GET)
    public String getSchool(Model model){
        model.addAttribute("name","武汉科技大学");
        model.addAttribute("age",122);
        return "/demo/view";
    }

    //  响应JSON数据(异步请求)
    // JAVA对象 -> JSON字符串 -> JS对象
    @RequestMapping(path = "/emp",method = RequestMethod.GET)
    @ResponseBody
    public Map<String,Object> getEmp(){
        Map<String, Object> mapEmp = new HashMap<>();
        mapEmp.put("name","张三");
        mapEmp.put("age",23);
        mapEmp.put("salary",8000.00);
        return mapEmp;
    }

    @RequestMapping(path = "/emps",method = RequestMethod.GET)
    @ResponseBody
    public List<Map<String,Object>> getEmps(){
        List<Map<String,Object>> list=new ArrayList<>();
        for(int i=0,salary=8000;i<3;i++,salary+=1000){
            Map<String, Object> mapEmp = new HashMap<>();
            mapEmp.put("name","张三");
            mapEmp.put("age",23);
            mapEmp.put("salary",salary);
            list.add(mapEmp);
        }
        return list;
    }

    @RequestMapping(path = "/cookie/set",method = RequestMethod.GET)
    @ResponseBody
    public String setCookie(HttpServletResponse response){
        Cookie cookie=new Cookie("code", CommunityUtil.generateUUID());
        /*设置cookie的生效路径，即哪些请求下会携带cookie会*/
        cookie.setPath("/community/alpha");
        /* cooki的超时时间 */
        cookie.setMaxAge(60*10);
        response.addCookie(cookie);

        return "set Cookie";
    }


    /*如果 CookieValue不存在,取不到会报错*/
    @RequestMapping(path = "/cookie/get",method = RequestMethod.GET)
    @ResponseBody
    public String getCookie(@CookieValue(value = "code",required = false,defaultValue = "该cookie不存在") String code){
        System.out.println(code);
        return "get Cookie";
    }

    /* session示例
    * 1、粘性session，用户第一次访问时，哪一台服务器给存了该用户的session，该用户以后访问都走这台服务器
    *    缺点：不是均匀分配的，负载不能均衡
    *
    * 2、同步session，每一台服务器存储session时，同步给其他服务器，但是服务器压力会很大，而且服务器与服务器之间有耦合，有关联
    *
    * 3、共享session，设置一台专门存session的服务器，但是这台服务器挂了，就完了，如果设置多台session服务器，和同步session又没区别
    *
    * 4、改进方法三，用数据库来当这台session服务器，数据库集群技术比较成熟，到时sql数据库是在硬盘中，访问速度慢，所以可以用
    *   nosql数据库，Redis数据库来做这个session服务器
    *
    *  */
    @RequestMapping(path = "/session/set",method = RequestMethod.GET)
    @ResponseBody
    public String setSession(HttpSession session){
        session.setAttribute("id",1);
        session.setAttribute("name","Test");
        return "set Session";
    }

    @RequestMapping(path = "/session/get",method = RequestMethod.GET)
    @ResponseBody
    public String getSession(HttpSession session){
        System.out.println(session.getAttribute("id"));
        System.out.println(session.getAttribute("name"));
        return "get Session";
    }

    /* ajax示例 */
    @RequestMapping(path = "/ajax",method = RequestMethod.POST)
    @ResponseBody
    public String testAjax(String name,int age){
        System.out.println(name);
        System.out.println(age);
        return CommunityUtil.getJSONString(0,"操作成功");
    }

}
