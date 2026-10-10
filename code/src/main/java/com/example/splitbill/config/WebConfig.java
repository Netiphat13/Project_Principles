package com.example.splitbill.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    public static final String USER_ID = "userId";

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // หน้าเว็บ: ยังไม่ล็อกอิน -> redirect ไปหน้า login
        registry.addInterceptor(new HandlerInterceptor() {
            @Override
            public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
                if (isLoggedIn(request)) return true;
                response.sendRedirect(request.getContextPath() + "/login");
                return false;
            }
        }).addPathPatterns("/home", "/bills", "/bills/**",
                "/stats", "/profile", "/profile/**");
        // หมายเหตุ: /join จัดการเองใน WebController (จำรหัสไว้ก่อนแล้วค่อยพาไป login)

        // REST API: ยังไม่ล็อกอิน -> 401 (ยกเว้นการสมัครสมาชิก POST /api/v1/users)
        registry.addInterceptor(new HandlerInterceptor() {
            @Override
            public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
                if (isLoggedIn(request) || isRegistration(request)) return true;
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                response.getWriter().write("{\"status\":401,\"error\":\"Unauthorized\",\"message\":\"Login required\"}");
                return false;
            }
        }).addPathPatterns("/api/**");
    }

    private static boolean isLoggedIn(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        return session != null && session.getAttribute(USER_ID) != null;
    }

    private static boolean isRegistration(HttpServletRequest request) {
        return "POST".equalsIgnoreCase(request.getMethod())
                && (request.getContextPath() + "/api/v1/users").equals(request.getRequestURI());
    }
}
