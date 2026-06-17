package com.invision.web.Invision.controller.pages;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class UsersPageController {
    @GetMapping("/users")
    public String usersPage(Model model) {
        model.addAttribute("currentUri", "/users");
        return "users/users";
    }
}