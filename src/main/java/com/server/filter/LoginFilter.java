package com.server.filter;


import com.server.pojo.entity.UserInfo;
import com.server.util.UserThreadLocal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
@Slf4j
public class LoginFilter implements Filter {



    @Override
    public void doFilter(ServletRequest request, ServletResponse response,
                         FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpServletRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        UserInfo userInfo = new UserInfo();
        userInfo.setUserName("chenhu");
        userInfo.setPhone("12222222222");
        UserThreadLocal.set(userInfo);

        chain.doFilter(request, httpResponse);


    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void destroy() {
        UserThreadLocal.remove();
    }

}
