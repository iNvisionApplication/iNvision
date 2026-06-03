package com.invision.web.Invision.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long privateId;
    private String name;
    private String department;
    private String email;
    private String password;
    private Role role;

}
