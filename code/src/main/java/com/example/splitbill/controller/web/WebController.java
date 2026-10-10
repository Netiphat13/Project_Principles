package com.example.splitbill.controller.web;

import com.example.splitbill.config.WebConfig;
import com.example.splitbill.dto.request.UserRequest;
import com.example.splitbill.exception.ConflictException;
import com.example.splitbill.exception.InvalidCredentialsException;
import com.example.splitbill.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * หน้าเว็บ Thymeleaf — ชื่อ view อ้างอิงไฟล์ใน templates/html/ (ตั้ง prefix ไว้ใน application.properties)
 * ข้อมูลบิล/โปรไฟล์ในแต่ละหน้าโหลดผ่าน REST API (/api/v1/**) ด้วย static/js/app.js
 */
@Controller
public class WebController {
    private static final String USER_ID = WebConfig.USER_ID;
    // รหัสเข้าร่วมบิลที่รอไว้ระหว่างพาผู้ใช้ไปล็อกอิน
    private static final String PENDING_JOIN = "pendingJoinCode";

    private final UserService userService;

    public WebController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/")
    public String root(HttpSession session) {
        return session.getAttribute(USER_ID) == null ? "redirect:/login" : "redirect:/home";
    }

    @GetMapping("/login")
    public String loginPage() { return "login"; }

    @PostMapping("/login")
    public String login(@RequestParam String email, @RequestParam String password,
                        HttpServletRequest request, RedirectAttributes ra) {
        try {
            var user = userService.authenticate(email, password);
            // สร้าง session ใหม่หลังล็อกอิน ป้องกัน session fixation
            HttpSession old = request.getSession(false);
            Object pendingJoin = old == null ? null : old.getAttribute(PENDING_JOIN);
            if (old != null) old.invalidate();
            HttpSession session = request.getSession(true);
            session.setAttribute(USER_ID, user.id());
            session.setAttribute("username", user.username());
            session.setAttribute("email", user.email());
            return pendingJoin == null ? "redirect:/home" : "redirect:" + joinUrl(pendingJoin.toString());
        } catch (InvalidCredentialsException ex) {
            ra.addFlashAttribute("error", "อีเมลหรือรหัสผ่านไม่ถูกต้อง");
            ra.addFlashAttribute("email", email); // คงอีเมลที่กรอกไว้ในฟอร์ม
            return "redirect:/login";
        }
    }

    @GetMapping("/register")
    public String registerPage() { return "register"; }

    @PostMapping("/register")
    public String register(@Valid @ModelAttribute UserRequest form, BindingResult result, RedirectAttributes ra) {
        if (result.hasErrors()) {
            ra.addFlashAttribute("error", "ข้อมูลไม่ถูกต้อง: ชื่อห้ามว่าง อีเมลต้องถูกรูปแบบ และรหัสผ่านอย่างน้อย 8 ตัวอักษร");
            return "redirect:/register";
        }
        try {
            userService.create(form);
            ra.addFlashAttribute("success", "สมัครสมาชิกสำเร็จ กรุณาเข้าสู่ระบบ");
            return "redirect:/login";
        } catch (ConflictException ex) {
            ra.addFlashAttribute("error", "อีเมลนี้ถูกใช้สมัครแล้ว");
            return "redirect:/register";
        }
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }

    // ลิงก์เชิญ /join?code=SM-XXXXXX — เปิดหน้าบิลของฉันพร้อมกล่องกรอกรหัส
    @GetMapping("/join")
    public String join(@RequestParam(required = false) String code, HttpSession session) {
        String clean = code == null ? "" : code.trim();
        if (session.getAttribute(USER_ID) == null) {
            if (!clean.isEmpty()) session.setAttribute(PENDING_JOIN, clean);
            return "redirect:/login";
        }
        return clean.isEmpty() ? "redirect:/bills" : "redirect:" + joinUrl(clean);
    }

    @GetMapping("/home")
    public String home() { return "index"; }

    @GetMapping("/bills")
    public String bills() { return "bills"; }

    @GetMapping("/bills/new")
    public String newBill(Model model, @SessionAttribute(USER_ID) Long userId) {
        model.addAttribute("userId", userId);
        return "create-bill";
    }

    // หน้า bill-detail โหลดข้อมูลจาก /api/v1/bills/{id} ด้วย ?id=
    @GetMapping("/bills/detail")
    public String billDetailPage() { return "bill-detail"; }

    @GetMapping("/stats")
    public String stats() { return "stats"; }

    @GetMapping("/profile")
    public String profile() { return "profile"; }

    @GetMapping("/profile/edit")
    public String editProfile() { return "edit-profile"; }

    private static String joinUrl(String code) {
        return UriComponentsBuilder.fromPath("/bills").queryParam("join", code).encode().toUriString();
    }
}
