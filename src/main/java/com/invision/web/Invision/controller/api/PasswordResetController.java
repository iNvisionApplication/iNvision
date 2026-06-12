package com.invision.web.Invision.controller.api;

import com.invision.web.Invision.service.PasswordResetService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/forgot-password")
@RequiredArgsConstructor
public class PasswordResetController {

    private final PasswordResetService passwordResetService;

    @GetMapping
    public String showForgotPasswordForm() {
        return "auth/forgot-password";
    }

    @PostMapping
    public String handleForgotPasswordRequest(@RequestParam String email, RedirectAttributes redirectAttributes) {
        passwordResetService.createResetToken(email);

        // Flash message remains generic for user enumeration protection
        redirectAttributes.addFlashAttribute("infoMessage",
                "If that email matches an account in our system, a password reset link has been sent.");
        return "redirect:/forgot-password";
    }

    @GetMapping("/reset")
    public String showResetPasswordForm(@RequestParam String token, Model model) {
        try {
            passwordResetService.validateToken(token);
            model.addAttribute("token", token);
            return "auth/reset-password";
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "auth/forgot-password"; // Redirect back with error message details
        }
    }

    @PostMapping("/reset")
    public String handlePasswordResetExecution(
            @RequestParam String token,
            @RequestParam String password,
            @RequestParam String confirmPassword,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (!password.equals(confirmPassword)) {
            model.addAttribute("token", token);
            model.addAttribute("errorMessage", "Passwords do not match.");
            return "auth/reset-password";
        }

        if (password.length() < 8) {
            model.addAttribute("token", token);
            model.addAttribute("errorMessage", "Password must be at least 8 characters long.");
            return "auth/reset-password";
        }

        try {
            passwordResetService.updatePassword(token, password);
            redirectAttributes.addFlashAttribute("successMessage", "Password updated successfully. Please log in.");
            return "redirect:/login";
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "auth/forgot-password";
        }
    }
}
