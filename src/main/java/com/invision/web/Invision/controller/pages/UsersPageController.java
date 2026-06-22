package com.invision.web.Invision.controller.pages;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class UsersPageController {
    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public String usersPage(Model model) {
        model.addAttribute("currentUri", "/users");
        return "users/users";
    }
}