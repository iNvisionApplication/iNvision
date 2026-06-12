package com.invision.web.Invision.dto;

import com.invision.web.Invision.model.Role;

public record UserRegistrationDto(String name,String email,String department, String password, Role role) {}


