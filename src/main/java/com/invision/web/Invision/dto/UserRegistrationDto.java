package com.invision.web.Invision.dto;

import com.invision.web.Invision.enums.Role;

public record UserRegistrationDto(
        String name,
        String department,
        String email,
        String password,
        Role role
) {}