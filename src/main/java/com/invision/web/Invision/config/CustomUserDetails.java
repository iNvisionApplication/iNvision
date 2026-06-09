package com.invision.web.Invision.config;

import com.invision.web.Invision.model.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

public class CustomUserDetails implements UserDetails {

    private final User user; // Your database User entity

    public CustomUserDetails(User user) {
        this.user = user;
    }

    // This is the magic method that lets you grab the database ID later!
    public Long getId() {
        return user.getUserId(); // Adjust based on your User entity's primary key getter
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Maps your DB role/RBAC to Spring Security authorities
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + user.getRole()));
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getUsername(); // or user.getEmail() depending on your login strategy
    }

    @Override
    public boolean isAccountNonExpired() { return true; }

    @Override
    public boolean isAccountNonLocked() { return true; }

    @Override
    public boolean isCredentialsNonExpired() { return true; }

    @Override
    public boolean isEnabled() { return true; }
}