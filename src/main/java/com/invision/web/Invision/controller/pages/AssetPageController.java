package com.invision.web.Invision.controller.pages;

import com.invision.web.Invision.config.CustomUserDetails;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AssetPageController {

    @GetMapping("/assets")
    public String assetsPage(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            Model model) {

        // 1. Extract the role string from Spring Security authorities (e.g., "ROLE_ADMIN" -> "ADMIN")
        String role = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .findFirst()
                .orElse("BORROWER")
                .replace("ROLE_", "");

        // 2. Bind all attributes to the Thymeleaf Model context
        model.addAttribute("currentUserId", userDetails.getId());
        model.addAttribute("currentUserRole", role); // <-- THIS WAS MISSING
        model.addAttribute("currentUri", "/assets");

        return "assets/assets";
    }

    @GetMapping("/assets/add")
    public String addAssetPage(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        model.addAttribute("currentUserId", userDetails.getId());
        model.addAttribute("currentUri", "/assets");

        return "assets/add-assets";
    }

}