package com.invision.web.Invision.controller;

import com.invision.web.Invision.dto.UserRegistrationDto;
import com.invision.web.Invision.service.UserService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // Path: /api/user/admin/create-staff
    @PostMapping("/admin/create-staff")
    public String createElevatedUser(@RequestBody UserRegistrationDto request) {
        return userService.createStaffUser(request);
    }

    // Path: /api/user/register
    @PostMapping("/register")
    public String registerUser(@RequestBody UserRegistrationDto request) {
        return userService.registerUser(request);
    }

    // Path: /api/user/public/home
    @GetMapping("/public/home")
    public String publicAccess() {
        return "Public Content: LANDING PAGE.";
    }

    // Path: /api/user/borrower/home
    @GetMapping("/borrower/home")
    public String borrowerAccess() {
        return "Borrower Content: Borrowers can see this.";
    }

    // Path: /api/user/manager/home
    @GetMapping("/manager/home")
    public String managerAccess() {
        return "Manager Content: Managers can see this.";
    }

    // Path: /api/user/admin/home
    @GetMapping("/admin/home")
    public String adminAccess() {
        return "Admin Content: ONLY Admins can see this.";
    }
}