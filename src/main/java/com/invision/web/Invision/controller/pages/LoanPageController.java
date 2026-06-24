package com.invision.web.Invision.controller.pages;

import com.invision.web.Invision.config.CustomUserDetails;
import com.invision.web.Invision.enums.LoanStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class LoanPageController {

    @GetMapping("/loans")
    public String loansPage(
            @RequestParam(required = false) LoanStatus status,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            Model model) {

        model.addAttribute("currentUri", "/loans");
        model.addAttribute("statusFilter", status);
        model.addAttribute("loanFilter", "OVERDUE");
        model.addAttribute("department", userDetails.getDepartment());

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