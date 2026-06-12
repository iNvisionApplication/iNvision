package com.invision.web.Invision.service;

import com.invision.web.Invision.dto.UserLoginDTO;
import com.invision.web.Invision.dto.UserRegistrationDTO;
import com.invision.web.Invision.enums.Department;
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

    @Transactional(readOnly = true)
    public User loginUser(UserLoginDTO loginRequest) {

        User user = userRepository.findByEmail(loginRequest.email())
                .orElseThrow(() -> new RuntimeException("Invalid email or password."));

        if (!passwordEncoder.matches(loginRequest.password(), user.getPassword())) {
            throw new RuntimeException("Invalid email or password.");
        }

        return user;
    }

    @Transactional
    public String registerUser(UserRegistrationDTO request){

        User user = new User();

        user.setName(request.name());
        user.setDepartment(request.department());
        user.setEmail(request.email());

        user.setPassword(passwordEncoder.encode(request.password()));
        user.setRole(Role.BORROWER);
        userRepository.save(user);
        return "User " + user.getEmail() + " registered successfully as " + user.getRole();
    }

    @Transactional
    public String createStaffUser(UserRegistrationDTO request) {

        User user = new User();

        user.setName(request.name());
        user.setDepartment(request.department());
        user.setEmail(request.email());

        user.setPassword(passwordEncoder.encode(request.password()));
        user.setRole(request.role());
        userRepository.save(user);
        return "User " + user.getEmail() + " registered successfully as " + user.getRole();
    }
}
