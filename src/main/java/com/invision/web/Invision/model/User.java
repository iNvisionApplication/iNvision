package com.invision.web.Invision.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Builder
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long userId;

    @NotNull
    private String name;

    private String department;

    @Email
    @NotNull
    private String email;

    @Enumerated(EnumType.STRING)
    private Role role;

    private String passwordHash;



}
