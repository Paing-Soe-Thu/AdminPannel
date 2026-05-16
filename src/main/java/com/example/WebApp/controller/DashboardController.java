package com.example.WebApp.controller;

import com.example.WebApp.model.User;
import com.example.WebApp.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DashboardController {

    private final UserService userService;

    public DashboardController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) {
        User loggedIn = (User) session.getAttribute("loggedInUser");
        model.addAttribute("loggedInUser", loggedIn);
        model.addAttribute("totalUsers", userService.countUsers());
        model.addAttribute("activePage", "dashboard");
        return "dashboard";
    }
}
