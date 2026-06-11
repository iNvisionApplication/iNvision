package com.invision.web.Invision.dto;

import com.invision.web.Invision.enums.Department;
import com.invision.web.Invision.enums.Role;
import jakarta.validation.constraints.*;

public record UserRegistrationDTO(
        @NotBlank(message = "Full name is required")
        @Size(max = 100, message = "Name must not exceed 100 characters")
        String name,

        @NotNull(message = "Department is required") // Enums use @NotNull, not @NotBlank
        Department department,

        @Email(message = "Enter a valid email address")
        @NotBlank(message = "Email is required")
        String email,

        @NotBlank(message = "Password is required")
        @Size(min = 8, message = "Password must be at least 8 characters")
        @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d@$!%*#?&]{8,}$",
                message = "Password must contain at least one letter and one number")
        String password,


        String confirmPassword,

        Role role
) {}