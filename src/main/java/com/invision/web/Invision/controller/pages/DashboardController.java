package com.invision.web.Invision.controller.pages;

import com.invision.web.Invision.config.CustomUserDetails;
import com.invision.web.Invision.enums.AssetStatus;
import com.invision.web.Invision.enums.LoanStatus;
import com.invision.web.Invision.enums.Role;
import com.invision.web.Invision.repository.AssetRepository;
import com.invision.web.Invision.repository.LoanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.LocalDateTime;

@Controller
@RequiredArgsConstructor
public class DashboardController {

    private final AssetRepository assetRepository;
    private final LoanRepository loanRepository;

    @GetMapping("/dashboard")
    public String dashboard(Model model, Authentication authentication) {

        if (authentication == null || !(authentication.getPrincipal() instanceof CustomUserDetails currentUser)) {
            return "redirect:/login";
        }

        Long userId = currentUser.getId();
        Role role = currentUser.getUser().getRole();

        model.addAttribute("currentUri", "/dashboard");

        // same for everyone
        model.addAttribute("totalAssets", assetRepository.count());
        model.addAttribute("availableAssets",
                assetRepository.countByStatus(AssetStatus.AVAILABLE));

        if (role == Role.BORROWER) {

            model.addAttribute("activeLoans",
                    loanRepository.countByUserUserIdAndStatus(userId, LoanStatus.APPROVED));

            model.addAttribute("pendingRequests",
                    loanRepository.countByUserUserIdAndStatus(userId, LoanStatus.PENDING));

            model.addAttribute("overdueLoans",
                    loanRepository.countByUserUserIdAndStatusAndDueDateBefore(
                            userId,
                            LoanStatus.APPROVED,
                            LocalDateTime.now()
                    ));

            model.addAttribute("recentLoans",
                    loanRepository.findTop5ByUserUserIdOrderByRequestDateDesc(userId));

        } else {

            model.addAttribute("activeLoans",
                    loanRepository.countByStatus(LoanStatus.APPROVED));

            model.addAttribute("pendingRequests",
                    loanRepository.countByStatus(LoanStatus.PENDING));

            model.addAttribute("overdueLoans",
                    loanRepository.countByStatusAndDueDateBefore(
                            LoanStatus.APPROVED,
                            LocalDateTime.now()
                    ));

            model.addAttribute("recentLoans",
                    loanRepository.findTop5ByOrderByRequestDateDesc());
        }

        return "dashboard/dashboard";
    }
}