package com.invision.web.Invision.controller.api;

import com.invision.web.Invision.dto.UserRegistrationDTO;
import com.invision.web.Invision.enums.Role;
import com.invision.web.Invision.exception.user.EmailAlreadyExistsException;
import com.invision.web.Invision.exception.user.PasswordMismatchException;
import com.invision.web.Invision.service.UserService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;

@Controller
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @GetMapping("/")
    public String root(HttpSession session, Authentication authentication) {

        if (isLoggedIn(authentication)) {
            return "redirect:/dashboard";
        }

        session.setAttribute("splashSeen", true);
        return "splash";
    }

    @GetMapping("/login")
    public String login(HttpSession session, Authentication authentication) {

        if (isLoggedIn(authentication)) {
            return "redirect:/dashboard";
        }

        Boolean splashSeen = (Boolean) session.getAttribute("splashSeen");

        if (splashSeen == null || !splashSeen) {
            return "redirect:/";
        }

        return "auth/login";
    }

    @GetMapping("/register")
    public String registerForm(Model model, HttpSession session, Authentication authentication) {

        if (isLoggedIn(authentication)) {
            return "redirect:/dashboard";
        }

        Boolean splashSeen = (Boolean) session.getAttribute("splashSeen");

        if (splashSeen == null || !splashSeen) {
            return "redirect:/";
        }

        model.addAttribute(
                "userRegistrationDTO",
                new UserRegistrationDTO("", "", null, "", "", Role.BORROWER)
        );

        return "auth/register";
    }

    @PostMapping("/register")
    public String registerSubmit(
            @Valid @ModelAttribute("userRegistrationDTO") UserRegistrationDTO dto,
            BindingResult bindingResult,
            Model model) {

        if (bindingResult.hasErrors()) {
            return "auth/register";
        }

        if (!Objects.equals(dto.password(), dto.confirmPassword())) {
            bindingResult.rejectValue("confirmPassword", "error.userRegistrationDTO", "Passwords do not match");
            return "auth/register";
        }

        try {
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

    @GetMapping("/api/auth/keep-alive")
    public ResponseEntity<Void> keepSessionAlive() {
        return ResponseEntity.ok().build();
    }

    private boolean isLoggedIn(Authentication authentication) {
        return authentication != null
                && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken);
    }
}