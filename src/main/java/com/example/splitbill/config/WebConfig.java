package com.example.splitbill.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new HandlerInterceptor() {
            @Override
            public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
                if (request.getSession().getAttribute("userId") == null) {
                    if (request.getRequestURI().startsWith("/api/")) {
                        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    } else {
                        response.sendRedirect("/login");
                    }
                    return false;
                }
                return true;
            }
        }).addPathPatterns("/home", "/index.html", "/groups", "/groups.html", "/group-detail", "/group-detail.html", "/bills", "/bills.html", "/bill-detail", "/bill-detail.html", "/create-bill", "/create-bill.html", "/stats", "/stats.html", "/profile", "/profile.html", "/edit-profile", "/edit-profile.html", "/api/v1/**")
          .excludePathPatterns("/login", "/register");
    }
}
