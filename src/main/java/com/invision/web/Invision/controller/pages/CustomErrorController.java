package com.invision.web.Invision.controller.pages;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.webmvc.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
@Controller
public class CustomErrorController implements ErrorController {

    @RequestMapping("/error")
    public String handleError(HttpServletRequest request, Model model) {
        Integer statusCode = (Integer) request.getAttribute(
                RequestDispatcher.ERROR_STATUS_CODE);

        System.out.println("Error status code: " + statusCode);

        // read the actual error message from the request
        String errorMessage = (String) request.getAttribute(
                RequestDispatcher.ERROR_MESSAGE);
        String requestUri = (String) request.getAttribute(
                RequestDispatcher.ERROR_REQUEST_URI);

        if (statusCode == null) {
            model.addAttribute("statusCode", "Error");
            model.addAttribute("heading", "Something went wrong");
            model.addAttribute("message", "An unexpected error has occurred.");
            return "error/error";
        }

        switch (statusCode) {
            case 403 -> {
                model.addAttribute("statusCode", "403");
                model.addAttribute("heading", "Access Denied");
                model.addAttribute("message", "You don't have permission to view this page.");
            }
            case 404 -> {
                model.addAttribute("statusCode", "404");
                model.addAttribute("heading", "Page Not Found");
                model.addAttribute("message", "The page you're looking for doesn't exist.");
            }
            case 500 -> {
                model.addAttribute("statusCode", "500");
                model.addAttribute("heading", "Server Error");
                model.addAttribute("message", "Something went wrong on our end. Please try again later.");
            }
            default -> {
                model.addAttribute("statusCode", statusCode.toString());
                model.addAttribute("heading", "Unexpected Error");
                model.addAttribute("message", "An unexpected error has occurred.");
            }
        }

        model.addAttribute("pageTitle", "Error " + statusCode);
        return "error/error";
    }
}