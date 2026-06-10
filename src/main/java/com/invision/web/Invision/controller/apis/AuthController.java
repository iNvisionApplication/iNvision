package com.invision.web.Invision.controller.apis;

import com.invision.web.Invision.dto.UserRegistrationDTO;
import com.invision.web.Invision.enums.Department;
import com.invision.web.Invision.enums.Role;
import com.invision.web.Invision.exceptions.EmailAlreadyExistsException;
import com.invision.web.Invision.exceptions.PasswordMismatchException;
import com.invision.web.Invision.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import java.util.Objects;


@Controller
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @GetMapping("/")
    public String root() {
        return "redirect:/login";
    }

    @GetMapping("/login")
    public String login() {
        return "auth/login";
    }

    @GetMapping("/register")
    public String registerForm(Model model) {

        model.addAttribute("nameError", null);
        model.addAttribute("departmentError", null);
        model.addAttribute("emailError", null);
        model.addAttribute("passwordError", null);
        model.addAttribute("confirmPasswordError", null);
        model.addAttribute("roleError", null);

        return "auth/register";
    }

    @PostMapping("/register")
    public String registerSubmit(
            @RequestParam String name,
            @RequestParam String department,
            @RequestParam String email,
            @RequestParam String password,
            @RequestParam String confirmPassword,
            Model model) {

        boolean hasError = false;

        if (name == null || name.isBlank()) {
            model.addAttribute("nameError", "Name is required");
            hasError = true;
        }

        if (department == null || department.isBlank()) {
            model.addAttribute("departmentError", "Department is required");
            hasError = true;
        }

        if (email == null || email.isBlank() || !email.contains("@")) {
            model.addAttribute("emailError", "Valid email is required");
            hasError = true;
        }

        if (password == null || password.length() < 8) {
            model.addAttribute("passwordError", "Password must be at least 8 characters");
            hasError = true;
        }


        if (!Objects.equals(password, confirmPassword)) {
            model.addAttribute("confirmPasswordError", "Passwords do not match");
            hasError = true;
        }


        if (hasError) {
            model.addAttribute("name", name);
            model.addAttribute("department", department);
            model.addAttribute("email", email);
            return "auth/register";
        }

        UserRegistrationDTO dto = new UserRegistrationDTO(
                name,
                Department.valueOf(department),
                email,
                password,
                confirmPassword,
                Role.BORROWER
        );

        try {
            userService.registerUser(dto);
        } catch (EmailAlreadyExistsException ex) {
            model.addAttribute("emailError", ex.getMessage());
            model.addAttribute("name", name);
            model.addAttribute("department", department);
            model.addAttribute("email", email);
            return "auth/register";
        } catch (PasswordMismatchException ex) {
            model.addAttribute("confirmPasswordError", ex.getMessage());
            model.addAttribute("name", name);
            model.addAttribute("department", department);
            model.addAttribute("email", email);
            return "auth/register";
        }

        return "redirect:/login?registered";
    }
}