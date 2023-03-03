package com.ljx.community.util;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

public class CookieUtil {
    public static String getValue(HttpServletRequest request,String name){
        /*
        * 判空！
        * */
        if(request==null || name==null){
            throw new IllegalArgumentException("CookieUtiles方法中的getValue方法参数为空！");
        }


        Cookie[] cookies = request.getCookies();
        if(cookies!=null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals(name))
                    return cookie.getValue();
            }
        }
        return null;
    }
}
