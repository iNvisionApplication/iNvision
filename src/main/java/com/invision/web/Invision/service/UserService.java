package com.invision.web.Invision.service;

import com.invision.web.Invision.dto.UserRegistrationDto;
import com.invision.web.Invision.model.Department;
import com.invision.web.Invision.model.Role;
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
    public String registerUser(UserRegistrationDto request){

        User user = new User();

        user.setName(request.name());
        user.setDepartment(Department.valueOf(request.department()));
        user.setEmail(request.email());

        user.setPassword(passwordEncoder.encode(request.password()));
        user.setRole(Role.BORROWER);
        userRepository.save(user);
        return "User " + user.getEmail() + " registered successfully as " + user.getRole();
    }

    @Transactional
    public String createStaffUser(UserRegistrationDto request) {

        User user = new User();

        user.setName(request.name());
        user.setDepartment(Department.valueOf(request.department()));
        user.setEmail(request.email());

        user.setPassword(passwordEncoder.encode(request.password()));
        user.setRole(request.role());
        userRepository.save(user);
        return "User " + user.getEmail() + " registered successfully as " + user.getRole();
    }
}
