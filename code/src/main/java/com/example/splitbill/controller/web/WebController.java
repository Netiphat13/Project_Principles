package com.example.splitbill.controller.web;

import com.example.splitbill.config.WebConfig;
import com.example.splitbill.dto.request.UserRequest;
import com.example.splitbill.dto.response.BillSummary;
import com.example.splitbill.exception.ConflictException;
import com.example.splitbill.exception.InvalidCredentialsException;
import com.example.splitbill.service.BillService;
import com.example.splitbill.service.GroupService;
import com.example.splitbill.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * หน้าเว็บ Thymeleaf — ชื่อ view อ้างอิงไฟล์ใน templates/html/ (ตั้ง prefix ไว้ใน application.properties)
 */
@Controller
public class WebController {
    private static final String USER_ID = WebConfig.USER_ID;
    private static final Sort NEWEST_FIRST = Sort.by(Sort.Direction.DESC, "createdAt");

    private final UserService userService;
    private final GroupService groupService;
    private final BillService billService;

    public WebController(UserService userService, GroupService groupService, BillService billService) {
        this.userService = userService;
        this.groupService = groupService;
        this.billService = billService;
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
            if (old != null) old.invalidate();
            HttpSession session = request.getSession(true);
            session.setAttribute(USER_ID, user.id());
            session.setAttribute("username", user.username());
            return "redirect:/home";
        } catch (InvalidCredentialsException ex) {
            ra.addFlashAttribute("error", "อีเมลหรือรหัสผ่านไม่ถูกต้อง");
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

    @GetMapping("/home")
    public String home(Model model, @SessionAttribute(USER_ID) Long userId, HttpSession session) {
        model.addAttribute("username", session.getAttribute("username"));
        model.addAttribute("groups", groupService.findByCreator(userId, PageRequest.of(0, 6, NEWEST_FIRST)).getContent());
        model.addAttribute("bills", billService.findByCreator(userId, PageRequest.of(0, 6, NEWEST_FIRST)).getContent());
        return "index";
    }

    @GetMapping("/groups")
    public String groups(Model model, @SessionAttribute(USER_ID) Long userId) {
        model.addAttribute("groups", groupService.findByCreator(userId, PageRequest.of(0, 20, NEWEST_FIRST)).getContent());
        model.addAttribute("userId", userId);
        return "groups";
    }

    // หน้า group-detail ปัจจุบันอ่านข้อมูลจาก localStorage ด้วย ?id=
    @GetMapping("/groups/detail")
    public String groupDetailPage() { return "group-detail"; }

    @GetMapping("/groups/{id}")
    public String groupDetail(@PathVariable Long id, Model model, @SessionAttribute(USER_ID) Long userId) {
        model.addAttribute("group", groupService.getById(id, userId)); // 403 ถ้าไม่ใช่เจ้าของ/สมาชิก
        return "group-detail";
    }

    @GetMapping("/bills")
    public String bills(Model model, @SessionAttribute(USER_ID) Long userId) {
        model.addAttribute("bills", billService.findByCreator(userId, PageRequest.of(0, 30, NEWEST_FIRST)).getContent());
        return "bills";
    }

    @GetMapping("/bills/new")
    public String newBill(Model model, @SessionAttribute(USER_ID) Long userId) {
        model.addAttribute("userId", userId);
        return "create-bill";
    }

    // หน้า bill-detail ปัจจุบันอ่านข้อมูลจาก localStorage ด้วย ?id=
    @GetMapping("/bills/detail")
    public String billDetailPage() { return "bill-detail"; }

    @GetMapping("/stats")
    public String stats(Model model, @SessionAttribute(USER_ID) Long userId) {
        BillSummary summary = billService.summarize(userId);
        model.addAttribute("billCount", summary.billCount());
        model.addAttribute("total", summary.total());
        model.addAttribute("average", summary.average());
        return "stats";
    }

    @GetMapping("/profile")
    public String profile(Model model, @SessionAttribute(USER_ID) Long userId) {
        model.addAttribute("user", userService.getById(userId));
        return "profile";
    }

    @GetMapping("/profile/edit")
    public String editProfile(Model model, @SessionAttribute(USER_ID) Long userId) {
        model.addAttribute("user", userService.getById(userId));
        return "edit-profile";
    }
}
