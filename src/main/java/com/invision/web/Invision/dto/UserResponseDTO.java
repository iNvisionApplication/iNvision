package com.invision.web.Invision.dto;

import com.invision.web.Invision.enums.Role;

public record UserResponseDTO(String userId, String name, String department, String email, Role role) {
}
