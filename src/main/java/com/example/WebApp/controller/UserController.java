package com.example.WebApp.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.WebApp.dto.UserDto;
import com.example.WebApp.service.UserService;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

@Controller
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    /* ── List Users ─────────────────────────────────────────────────── */

    @GetMapping
    public String listUsers(HttpSession session, Model model) {
        model.addAttribute("users", userService.getAllUsers());
        model.addAttribute("totalUsers", userService.countUsers());
        model.addAttribute("loggedInUser", session.getAttribute("loggedInUser"));
        model.addAttribute("activePage", "users");
        return "users/list";
    }

    /* ── Add User ───────────────────────────────────────────────────── */

    @GetMapping("/add")
    public String addUserPage(HttpSession session, Model model) {
        model.addAttribute("userDto", new UserDto());
        model.addAttribute("loggedInUser", session.getAttribute("loggedInUser"));
        model.addAttribute("isEdit", false);
        model.addAttribute("activePage", "users");
        return "users/form";
    }

    @PostMapping("/add")
    public String addUser(@Valid @ModelAttribute("userDto") UserDto dto,
                          BindingResult result,
                          HttpSession session,
                          Model model,
                          RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("loggedInUser", session.getAttribute("loggedInUser"));
            model.addAttribute("isEdit", false);
            model.addAttribute("activePage", "users");
            return "users/form";
        }
        try {
            userService.registerUser(dto);
            redirectAttributes.addFlashAttribute("successMessage", "User added successfully!");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/users";
    }

    /* ── Edit User ──────────────────────────────────────────────────── */

    @GetMapping("/edit/{id}")
    public String editUserPage(@PathVariable Long id,
                               HttpSession session,
                               Model model,
                               RedirectAttributes redirectAttributes) {
        return userService.getUserById(id).map(user -> {
            UserDto dto = new UserDto();
            dto.setName(user.getName());
            dto.setEmail(user.getEmail());
            dto.setPhone(user.getPhone());
            // Password left blank intentionally (edit leaves existing unchanged)
            model.addAttribute("userDto", dto);
            model.addAttribute("userId", id);
            model.addAttribute("loggedInUser", session.getAttribute("loggedInUser"));
            model.addAttribute("isEdit", true);
            model.addAttribute("activePage", "users");
            return "users/form";
        }).orElseGet(() -> {
            redirectAttributes.addFlashAttribute("errorMessage", "User not found.");
            return "redirect:/users";
        });
    }

    @PostMapping("/edit/{id}")
    public String editUser(@PathVariable Long id,
                           @Valid @ModelAttribute("userDto") UserDto dto,
                           BindingResult result,
                           HttpSession session,
                           Model model,
                           RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("userId", id);
            model.addAttribute("loggedInUser", session.getAttribute("loggedInUser"));
            model.addAttribute("isEdit", true);
            model.addAttribute("activePage", "users");
            return "users/form";
        }
        try {
            userService.updateUser(id, dto);
            redirectAttributes.addFlashAttribute("successMessage", "User updated successfully!");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/users";
    }

    /* ── Delete User ────────────────────────────────────────────────── */

    @PostMapping("/delete/{id}")
    public String deleteUser(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        userService.deleteUser(id);
        redirectAttributes.addFlashAttribute("successMessage", "User deleted successfully!");
        return "redirect:/users";
    }
}
