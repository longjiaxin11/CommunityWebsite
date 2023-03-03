package com.ljx.community.config;

import com.ljx.community.util.CommunityConstant;
import com.ljx.community.util.CommunityUtil;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

@Configuration
public class SecurityConfig extends WebSecurityConfigurerAdapter implements CommunityConstant {

    /* 忽略对静态资源的拦截 */
    @Override
    public void configure(WebSecurity web) throws Exception {
        web.ignoring().antMatchers("/resources/**");
    }


    /* 授权 */
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        /* 授权 */
        /* 以下请求需要权限集合中的一个才可以访问，其他请求通通允许 */
        http.authorizeRequests()
                .antMatchers(
                    "/user/setting",
                    "/user/upload",
                    "/user/update",
                    "/comment/add/**",
                    "/discuss/add",
                    "/follow",
                    "/unfollow",
                    "/like",
                    "/letter/**",
                    "/notice/**"
                )
                .hasAnyAuthority(
                        AUTHORITY_USER,
                        AUTHORITY_ADMIN,
                        AUTHORITY_MODERATOR
                )
                .antMatchers(
                        "/discuss/top",
                        "/discuss/wonderful"
                )
                .hasAnyAuthority(
                    AUTHORITY_MODERATOR
                )
                .antMatchers(
                        "/discuss/delete",
                        "/data/**"
                )
                .hasAnyAuthority(
                    AUTHORITY_ADMIN
                )
                .anyRequest().permitAll()
                .and().csrf().disable();

        //权限不够时处理
        http.exceptionHandling()
                .authenticationEntryPoint(new AuthenticationEntryPoint() {
                    /* 没有登录时的处理 */
                    @Override
                    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {
                        String xRequestWith = request.getHeader("x-requested-with");
                        if(xRequestWith!=null && xRequestWith.equals("XMLHttpRequest")){
                            /* 请求是异步请求 */
                            response.setContentType("application/plain;charset=utf-8");
                            PrintWriter writer = response.getWriter();
                            writer.write(CommunityUtil.getJSONString(403,"你还没有登录！"));
                        }else{
                            response.sendRedirect(request.getContextPath()+"/login");
                        }
                    }
                })
                .accessDeniedHandler(new AccessDeniedHandler() {
                    /* 权限不足时的处理 */
                    @Override
                    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException, ServletException {
                        String xRequestWith = request.getHeader("x-requested-with");
                        if(xRequestWith!=null && xRequestWith.equals("XMLHttpRequest")){
                            /* 请求是异步请求 */
                            response.setContentType("application/plain;charset=utf-8");
                            PrintWriter writer = response.getWriter();
                            writer.write(CommunityUtil.getJSONString(403,"你还没有访问此功能的权限！"));
                        }else{
                            response.sendRedirect(request.getContextPath()+"/denied");
                        }
                    }
                });
        // Security底层默认拦截/logout这个请求，进行退出处理，并且拦截后终止，不走logout请求
        // 覆盖他默认的逻辑,并执行我们自己的退出代码
        /* 实现原理，改变security默认拦截的路径，覆盖掉原来的/logout */
        http.logout().logoutUrl("/securitylogout");
    }

}
