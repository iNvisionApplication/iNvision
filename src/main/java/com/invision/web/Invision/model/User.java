package com.invision.web.Invision.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;


@Entity
@Table(name="users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long userId;

    private String name;

    private String department;

    @Email
    private String email;

    @Enumerated(EnumType.STRING)
    private Role role;

    private String passwordHash;

}
