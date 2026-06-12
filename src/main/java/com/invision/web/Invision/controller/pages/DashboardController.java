package com.invision.web.Invision.controller.pages;

import com.invision.web.Invision.enums.AssetStatus;
import com.invision.web.Invision.enums.LoanStatus;
import com.invision.web.Invision.mapper.LoanMapper;
import com.invision.web.Invision.repository.AssetRepository;
import com.invision.web.Invision.repository.LoanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class DashboardController {

    private final AssetRepository assetRepository;
    private final LoanRepository loanRepository;
    private final LoanMapper loanMapper;

    @GetMapping("/dashboard")
    public String dashboard(Model model) {

        model.addAttribute("currentUri", "/dashboard");

        model.addAttribute("totalAssets", assetRepository.count());
        model.addAttribute("availableAssets", assetRepository.countByStatus(AssetStatus.AVAILABLE));
        model.addAttribute("retiredAssets", assetRepository.countByStatus(AssetStatus.RETIRED));

        model.addAttribute("activeLoans", loanRepository.countByStatus(LoanStatus.APPROVED));
        model.addAttribute("pendingRequests", loanRepository.countByStatus(LoanStatus.PENDING));


        model.addAttribute("overdueLoans", 0); //Using zero for now until full logic

        model.addAttribute(
                "recentLoans",
                loanRepository.findTop5ByOrderByRequestDateDesc()
                        .stream()
                        .map(loanMapper::loanToLoanResponseDTO)
                        .toList()
        );

        return "dashboard/dashboard";
    }
}