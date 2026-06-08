package com.invision.web.Invision.dto;

import com.invision.web.Invision.enums.Role;

public record UserRegistrationDTO(
        String name,
        String department,
        String email,
        String password,
        Role role
) {}