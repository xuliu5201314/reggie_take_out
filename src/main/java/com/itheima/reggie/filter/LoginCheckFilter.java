package com.itheima.reggie.filter;

import com.alibaba.fastjson.JSON;
import com.itheima.reggie.common.BaseContext;
import com.itheima.reggie.common.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.AntPathMatcher;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 检查用户是否完成登录
 */
@WebFilter(filterName="loginCheckFilter",urlPatterns = "/*" )
@Slf4j
public class LoginCheckFilter implements Filter {

    // 路径匹配器
    public static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        // 获取本次请求都得uri
        String requestURI = request.getRequestURI();

        log.info("拦截到的请求：{}",requestURI);

        String[] uris = new String[]{
                "/employee/login",
                "/employee/logout",
                "/backend/**",
                "/front/**",
                "/common/**",
                "/user/sendMsg",
                "/user/login"
        };

        // 判断请求是否需要处理
        boolean check = check(uris,requestURI);
        if (check){
            filterChain.doFilter(request,response);
            return;
        }

        /*1-1 判断登录状态，如果已登录，则直接放行*/
        if (request.getSession().getAttribute("employee") != null) {
            log.info("用户已经登录，用户的id为：{}",request.getSession().getAttribute("employee"));

            // 获取id 放入线程中
            Long empId = (Long) request.getSession().getAttribute("employee");
            BaseContext.setCurrentId(empId);

            filterChain.doFilter(request,response);
            return;
        }

        /*1-2 判断移动端登录状态，如果已登录，则直接放行*/
        if (request.getSession().getAttribute("user") != null) {
            log.info("用户已经登录，用户的id为：{}",request.getSession().getAttribute("user"));

            // 获取id 放入线程中
            Long userId = (Long) request.getSession().getAttribute("user");
            BaseContext.setCurrentId(userId);

            filterChain.doFilter(request,response);
            return;
        }

        // 如果未登录 返回未登录结果，通过输出溜的方式向客户端响应数据
        response.getWriter().write(JSON.toJSONString(R.error("NOTLOGIN")));
        return;

        //log.info("拦截到的请求：{}",request.getRequestURI());
    }

    /**
     * 路径匹配 检查本次请求是否需要放行
     * @param uris
     * @param requestURI
     * @return
     */
    public boolean check(String[] uris,String requestURI){
        for (String uri : uris){
            boolean match = PATH_MATCHER.match(uri, requestURI);

            if (match){
                return true;
            }
        }
        return false;
    }
}
