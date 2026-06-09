package com.invision.web.Invision.controller.apis;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import java.util.List;

@Controller
public class DashboardController {

    @GetMapping("/dashboard")
    public String dashboard(Model model) {

        model.addAttribute("currentUri", "/dashboard");

        model.addAttribute("totalAssets",     0);
        model.addAttribute("availableAssets", 0);
        model.addAttribute("activeLoans",     0);
        model.addAttribute("overdueLoans",    0);
        model.addAttribute("pendingRequests", 0);
        model.addAttribute("retiredAssets",   0);
        model.addAttribute("recentLoans",     List.of());
        return "dashboard/dashboard";
    }
}
