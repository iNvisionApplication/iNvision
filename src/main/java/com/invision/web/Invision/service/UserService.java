package com.invision.web.Invision.service;

import com.invision.web.Invision.dto.UserRegistrationDto;
import com.invision.web.Invision.enums.Role;
import com.invision.web.Invision.model.User;
import com.invision.web.Invision.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public void register(UserRegistrationDto dto) {

        if (!dto.password().equals(dto.confirmPassword())) {
            throw new IllegalArgumentException("Passwords do not match");
        }

        if (userRepository.existsByEmail(dto.email())) {
            throw new IllegalArgumentException("Email already exists");
        }

        User user = new User();

        user.setName(dto.name());
        user.setDepartment(dto.department());
        user.setEmail(dto.email());
        user.setPasswordHash(passwordEncoder.encode(dto.password()));
        user.setRole(Role.BORROWER);

        userRepository.save(user);
    }
}