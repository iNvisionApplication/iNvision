package com.invision.web.Invision.controller.pages;

import com.invision.web.Invision.config.CustomUserDetails;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LoanPageController {

    @GetMapping("/loans")
    public String loansPage(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            Model model) {

        model.addAttribute("currentUri", "/loans");

        model.addAttribute("currentUserId", userDetails.getId());
        model.addAttribute("currentUserRole",
                userDetails.getAuthorities()
                        .iterator()
                        .next()
                        .getAuthority()
                        .replace("ROLE_", ""));

        return "loans/loan";
    }
}