package com.invision.web.Invision.controller.pages;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LoanPageController {

    @GetMapping("/loans")
    public String loansPage() {
        return "loans/loan";
    }
}