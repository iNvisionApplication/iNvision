package com.invision.web.Invision.dto;

import com.invision.web.Invision.enums.Department;
import com.invision.web.Invision.enums.Role;
import jakarta.validation.constraints.*;

public record UserRegistrationDTO(

        @NotBlank(message = "Full name is required")
        String name,

        @NotBlank(message = "Department is required")
        Department department,

        @Email(message = "Enter a valid email address")
        @NotBlank(message = "Email is required")
        String email,

        @NotBlank(message = "Password is required")
        @Size(min = 8, message = "Password must be at least 8 characters")
        String password,

        // confirmPassword is not in the record therefore validated separately in controller
        String confirmPassword,

        Role role
) {}