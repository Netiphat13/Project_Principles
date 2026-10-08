package com.example.splitbill.controller.web;

import com.example.splitbill.dto.request.UserRequest;
import com.example.splitbill.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class WebController {
    private static final String USER_ID = "userId";

    private final UserService userService;

    public WebController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/")
    public String root(HttpSession session) {
        return session.getAttribute(USER_ID) == null ? "redirect:/login" : "redirect:/home";
    }

    @GetMapping("/login")
    public String loginPage() { return "auth/login"; }

    @PostMapping("/login")
    public String login(@RequestParam String email, @RequestParam String password,
                        HttpSession session, RedirectAttributes ra) {
        try {
            var user = userService.authenticate(email, password);
            session.setAttribute(USER_ID, user.id());
            session.setAttribute("username", user.username());
            return "redirect:/home";
        } catch (RuntimeException ex) {
            ra.addFlashAttribute("error", "อีเมลหรือรหัสผ่านไม่ถูกต้อง");
            return "redirect:/login";
        }
    }

    @GetMapping("/register")
    public String registerPage() { return "auth/register"; }

    @PostMapping("/register")
    public String register(@RequestParam String username, @RequestParam String email,
                           @RequestParam String password, RedirectAttributes ra) {
        try {
            userService.create(new UserRequest(username, email, password));
            ra.addFlashAttribute("success", "สมัครสมาชิกสำเร็จ กรุณาเข้าสู่ระบบ");
            return "redirect:/login";
        } catch (RuntimeException ex) {
            ra.addFlashAttribute("error", ex.getMessage());
            return "redirect:/register";
        }
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }

    @GetMapping("/home")
    public String home() { return "redirect:/index.html"; }

    @GetMapping("/groups")
    public String groups() { return "redirect:/groups.html"; }

    @GetMapping("/group-detail")
    public String groupDetail(@RequestParam Long id) { return "redirect:/group-detail.html?id=" + id; }

    @GetMapping("/bills")
    public String bills() { return "redirect:/bills.html"; }

    @GetMapping("/bill-detail")
    public String billDetail(@RequestParam Long id) { return "redirect:/bill-detail.html?id=" + id; }

    @GetMapping("/create-bill")
    public String createBill() { return "redirect:/create-bill.html"; }

    @GetMapping("/stats")
    public String stats() { return "redirect:/stats.html"; }

    @GetMapping("/profile")
    public String profile() { return "redirect:/profile.html"; }

    @GetMapping("/edit-profile")
    public String editProfile() { return "redirect:/edit-profile.html"; }
}
