package com.invision.web.Invision.service;

import com.invision.web.Invision.config.CustomUserDetails;
import com.invision.web.Invision.dto.UserLoginDTO;
import com.invision.web.Invision.dto.UserRegistrationDTO;
import com.invision.web.Invision.dto.UserUpdateDTO;
import com.invision.web.Invision.enums.Department;
import com.invision.web.Invision.enums.EntityType;
import com.invision.web.Invision.enums.Role;
import com.invision.web.Invision.exception.user.UserNotFoundException;
import com.invision.web.Invision.model.User;
import com.invision.web.Invision.repository.UserRepository;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditLogService auditLogService;


    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, AuditLogService auditLogService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.auditLogService = auditLogService;
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

    // ADMIN: DEACTIVATE USER
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Transactional
    public void deactivateUser(long userId){
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + userId));
        user.setActive(false);
        userRepository.save(user);
        auditLogService.logUpdate(getCurrentUserId(), EntityType.USER, userId, "Status: ACTIVE", "Status: DEACTIVATED");
    }

    // ADMIN: ACTIVATE USER
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public void activateUser(long userId){
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + userId));

        user.setActive(true);
        userRepository.save(user);

        auditLogService.logUpdate(
                getCurrentUserId(),
                EntityType.USER,
                userId,
                "Status: DEACTIVATED",
                "Status: ACTIVE"
        );
    }

    public Long getCurrentUserId() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails userDetails) {
            return userDetails.getId();
        }
        return null;
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Transactional
    public User updateUser(Long id, @Valid UserUpdateDTO updatedUser) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + id));

        user.setName(updatedUser.getName());
        user.setEmail(updatedUser.getEmail());
        user.setDepartment(updatedUser.getDepartment());
        user.setRole(updatedUser.getRole());

        return userRepository.save(user);
    }
}
