package com.example.splitbill.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.*;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new HandlerInterceptor() {
            @Override
            public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
                if (request.getSession().getAttribute("userId") == null) {
                    response.sendRedirect("/login");
                    return false;
                }
                return true;
            }
        }).addPathPatterns("/home", "/groups/**", "/bills/**", "/stats", "/profile")
          .excludePathPatterns("/login", "/register");
    }
}
