package com.invision.web.Invision.dto;

import com.invision.web.Invision.enums.Department;
import com.invision.web.Invision.enums.Role;

public record UserRegistrationDTO(String name, String email, Department department, String password, Role role) {}

