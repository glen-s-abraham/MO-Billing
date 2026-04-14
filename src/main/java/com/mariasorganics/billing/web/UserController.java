package com.mariasorganics.billing.web;

import com.mariasorganics.billing.model.Role;
import com.mariasorganics.billing.model.User;
import com.mariasorganics.billing.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @GetMapping
    public String listUsers(Model model) {
        model.addAttribute("users", userRepository.findAll());
        return "users-list";
    }

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("user", new User());
        model.addAttribute("roles", Role.values());
        return "user-form";
    }

    @PostMapping
    public String saveUser(@ModelAttribute User user) {
        if (user.getId() == null) {
            // New user, encode password
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        } else {
            // Update user, check if password was changed (for simplicity in this demo, we only set it if not empty)
            User existing = userRepository.findById(user.getId()).orElse(null);
            if (existing != null) {
                if (user.getPassword() != null && !user.getPassword().isEmpty()) {
                    user.setPassword(passwordEncoder.encode(user.getPassword()));
                } else {
                    user.setPassword(existing.getPassword());
                }
            }
        }
        userRepository.save(user);
        return "redirect:/users";
    }

    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable Long id, Model model) {
        User user = userRepository.findById(id).orElseThrow();
        user.setPassword(""); // Don't show password
        model.addAttribute("user", user);
        model.addAttribute("roles", Role.values());
        return "user-form";
    }

    @PostMapping("/{id}/toggle")
    public String toggleStatus(@PathVariable Long id) {
        User user = userRepository.findById(id).orElseThrow();
        user.setActive(!user.isActive());
        userRepository.save(user);
        return "redirect:/users";
    }
}
