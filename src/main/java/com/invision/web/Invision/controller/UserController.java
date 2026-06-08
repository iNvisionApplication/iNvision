package com.invision.web.Invision.controller;

import com.invision.web.Invision.dto.UserRegistrationDTO;
import com.invision.web.Invision.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/api")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/admin/create-staff")
    public String createElevatedUser(@RequestBody UserRegistrationDTO request) {
        return userService.createStaffUser(request);
    }


    @PostMapping("/register")
    public String registerUser(@RequestBody UserRegistrationDTO request) {
        return userService.registerUser(request);
    }


    @GetMapping("/home")
    public String publicAccess() {
        return "dashboard/dashboard";
    }


    @GetMapping("/borrower/home")
    public String borrowerAccess() {
        return "Borrower Content: Borrowers can see this.";
    }


    @GetMapping("/manager/home")
    public String managerAccess() {
        return "Manager Content: Managers can see this.";
    }

    @GetMapping("/admin/home")
    public String adminAccess() {
        return "Admin Content: ONLY Admins can see this.";
    }
}