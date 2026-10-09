package com.example.splitbill.controller.web;

import com.example.splitbill.dto.request.UserRequest;
import com.example.splitbill.service.BillService;
import com.example.splitbill.service.GroupService;
import com.example.splitbill.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class WebController {
    private static final String USER_ID = "userId";

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
    public String registerPage() { return "register"; }

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
    public String home(Model model, HttpSession session) {
        Long userId = (Long) session.getAttribute(USER_ID);
        model.addAttribute("username", session.getAttribute("username"));
        model.addAttribute("groups", groupService.findByCreator(userId, PageRequest.of(0, 6)).getContent());
        model.addAttribute("bills", billService.findByCreator(userId, PageRequest.of(0, 6)).getContent());
        return "home";
    }

    @GetMapping("/groups")
    public String groups(Model model, HttpSession session) {
        model.addAttribute("groups", groupService.findByCreator((Long) session.getAttribute(USER_ID), PageRequest.of(0, 20)).getContent());
        model.addAttribute("userId", session.getAttribute(USER_ID));
        return "groups";
    }

    @GetMapping("/groups/{id}")
    public String groupDetail(@PathVariable Long id, Model model, HttpSession session) {
        model.addAttribute("group", groupService.getById(id));
        return "group-detail";
    }

    @GetMapping("/bills")
    public String bills(Model model, HttpSession session) {
        Long userId = (Long) session.getAttribute(USER_ID);
        model.addAttribute("bills", billService.findByCreator(userId, PageRequest.of(0, 30)).getContent());
        return "bills";
    }

    @GetMapping("/bills/new")
    public String newBill(Model model, HttpSession session) {
        model.addAttribute("userId", session.getAttribute(USER_ID));
        return "bill-create";
    }

    @GetMapping("/stats")
    public String stats(Model model, HttpSession session) {
        Long userId = (Long) session.getAttribute(USER_ID);
        var bills = billService.findByCreator(userId, PageRequest.of(0, 100)).getContent();
        var total = bills.stream().map(b -> b.totalAmount() == null ? java.math.BigDecimal.ZERO : b.totalAmount())
                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);
        model.addAttribute("billCount", bills.size());
        model.addAttribute("total", total);
        model.addAttribute("average", bills.isEmpty() ? java.math.BigDecimal.ZERO :
                total.divide(java.math.BigDecimal.valueOf(bills.size()), 2, java.math.RoundingMode.HALF_UP));
        return "stats";
    }

    @GetMapping("/profile")
    public String profile(Model model, HttpSession session) {
        Long userId = (Long) session.getAttribute(USER_ID);
        model.addAttribute("user", userService.getById(userId));
        return "profile";
    }

}
