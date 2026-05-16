package com.example.WebApp.controller;

import com.example.WebApp.dto.LoginDto;
import com.example.WebApp.dto.UserDto;
import com.example.WebApp.model.User;
import com.example.WebApp.service.UserService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

@Controller
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    /* ── Login ─────────────────────────────────────────────────────── */

    @GetMapping({"/", "/login"})
    public String loginPage(HttpSession session, Model model) {
        // If already logged in, skip to dashboard
        if (session.getAttribute("loggedInUser") != null) {
            return "redirect:/dashboard";
        }
        model.addAttribute("loginDto", new LoginDto());
        return "login";
    }

    @PostMapping("/login")
    public String login(@Valid @ModelAttribute("loginDto") LoginDto dto,
                        BindingResult result,
                        HttpSession session,
                        Model model) {
        if (result.hasErrors()) {
            return "login";
        }
        Optional<User> userOpt = userService.authenticate(dto.getEmail(), dto.getPassword());
        if (userOpt.isEmpty()) {
            model.addAttribute("errorMessage", "Invalid email or password. Please try again.");
            return "login";
        }
        session.setAttribute("loggedInUser", userOpt.get());
        return "redirect:/dashboard";
    }

    /* ── Register ──────────────────────────────────────────────────── */

    @GetMapping("/register")
    public String registerPage(HttpSession session, Model model) {
        if (session.getAttribute("loggedInUser") != null) {
            return "redirect:/dashboard";
        }
        model.addAttribute("userDto", new UserDto());
        return "register";
    }

    @PostMapping("/register")
    public String register(@Valid @ModelAttribute("userDto") UserDto dto,
                           BindingResult result,
                           RedirectAttributes redirectAttributes,
                           Model model) {
        if (result.hasErrors()) {
            return "register";
        }
        try {
            userService.registerUser(dto);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Account created successfully! Please log in.");
            return "redirect:/login";
        } catch (IllegalArgumentException ex) {
            model.addAttribute("errorMessage", ex.getMessage());
            return "register";
        }
    }

    /* ── Logout ────────────────────────────────────────────────────── */

    @GetMapping("/logout")
    public String logout(HttpSession session, RedirectAttributes redirectAttributes) {
        session.invalidate();
        redirectAttributes.addFlashAttribute("successMessage", "You have been logged out.");
        return "redirect:/login";
    }
}
