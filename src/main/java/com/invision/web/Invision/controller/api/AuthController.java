package com.invision.web.Invision.controller.api;

import com.invision.web.Invision.config.CustomUserDetails;
import com.invision.web.Invision.dto.UserRegistrationDTO;
import com.invision.web.Invision.enums.Department;
import com.invision.web.Invision.enums.Role;
import com.invision.web.Invision.exception.user.EmailAlreadyExistsException;
import com.invision.web.Invision.exception.user.PasswordMismatchException;
import com.invision.web.Invision.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.Objects;

@Controller
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @GetMapping("/")
    public String root() {
        return "redirect:/login";
    }

    @GetMapping("/test")
    public String showTestPage(Model model) {
        var authentication = org.springframework.security.core.context.SecurityContextHolder
                .getContext()
                .getAuthentication();

        if (authentication == null) {
            model.addAttribute("authStatus", "No Authentication Object Found");
            model.addAttribute("principalClass", "N/A");
            model.addAttribute("username", "None");
            model.addAttribute("currentUserId", null);
        } else {
            Object principal = authentication.getPrincipal();

            model.addAttribute("authStatus", authentication.isAuthenticated() ? "Authenticated" : "Not Authenticated");
            model.addAttribute("principalClass", principal.getClass().getName());
            model.addAttribute("username", authentication.getName());

            if (principal instanceof com.invision.web.Invision.config.CustomUserDetails userDetails) {
                model.addAttribute("currentUserId", userDetails.getId());
            } else {
                model.addAttribute("currentUserId", null);
            }
        }
        return "dashboard/test";
    }

    private Long getCurrentUserId() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails userDetails) {
            return userDetails.getId();
        }
        return null;
    }

    @GetMapping("/login")
    public String login() {
        return "auth/login";
    }

    @GetMapping("/register")
    public String registerForm(Model model) {
        // Pass an empty DTO object so Thymeleaf form fields can bind to it out-of-the-box
        model.addAttribute("userRegistrationDTO", new UserRegistrationDTO("", null, "", "", "", Role.BORROWER));
        return "auth/register";
    }

    @PostMapping("/register")
    public String registerSubmit(
            @Valid @ModelAttribute("userRegistrationDTO") UserRegistrationDTO dto, // Automatically validates the DTO fields
            BindingResult bindingResult, // Holds validation errors automatically
            Model model) {

        // 1. Check for DTO validation annotation failures (@NotBlank, @Email, @Size)
        if (bindingResult.hasErrors()) {
            return "auth/register"; // Thymeleaf handles displaying these errors automatically via #fields
        }

        // 2. Cross-field validation manual check (Confirm Password)
        if (!Objects.equals(dto.password(), dto.confirmPassword())) {
            bindingResult.rejectValue("confirmPassword", "error.userRegistrationDTO", "Passwords do not match");
            return "auth/register";
        }

        try {
            // 3. Attempt service registration
            userService.registerUser(dto);
        } catch (EmailAlreadyExistsException ex) {
            bindingResult.rejectValue("email", "error.userRegistrationDTO", ex.getMessage());
            return "auth/register";
        } catch (PasswordMismatchException ex) {
            bindingResult.rejectValue("confirmPassword", "error.userRegistrationDTO", ex.getMessage());
            return "auth/register";
        }

        return "redirect:/login?registered";
    }
}