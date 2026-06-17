package com.invision.web.Invision.dto;

import com.invision.web.Invision.enums.Department;
import com.invision.web.Invision.enums.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserUpdateDTO {

    @NotBlank(message = "Name is required")
    private String name;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email address")
    private String email;

    @NotNull(message = "Department is required")
    private Department department;

    @NotNull(message = "Role is required")
    private Role role;

}