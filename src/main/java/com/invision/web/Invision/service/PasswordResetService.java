package com.invision.web.Invision.service;

import com.invision.web.Invision.model.PasswordResetToken;
import com.invision.web.Invision.model.User;
import com.invision.web.Invision.repository.PasswordResetTokenRepository;
import com.invision.web.Invision.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class PasswordResetService {

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;

    public void createResetToken(String email) {
        var userOpt = userRepository.findByEmail(email);

        if (userOpt.isEmpty()) {
            return;
        }

        User user = userOpt.get();
        String token = UUID.randomUUID().toString();

        PasswordResetToken resetToken = PasswordResetToken.builder()
                .token(token)
                .user(user)
                .expiryDate(LocalDateTime.now().plusMinutes(15)) // Valid for 15 minutes
                .used(false)
                .build();

        tokenRepository.save(resetToken);

        // NOTES!!!!!!!!!!!!!
        // 2. Output the link (Replace with emailService.sendAsync() when email sender is configured)
        System.out.println("Password reset link: http://localhost:8081/forgot-password/reset?token=" + token);
    }

    public PasswordResetToken validateToken(String token) {
        PasswordResetToken resetToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new IllegalArgumentException("The reset link is structurally invalid."));

        if (resetToken.isUsed()) {
            throw new IllegalStateException("This password link has already been used.");
        }

        if (resetToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new IllegalStateException("This validation token has expired.");
        }

        return resetToken;
    }

    public void updatePassword(String token, String newPassword) {
        PasswordResetToken resetToken = validateToken(token);
        User user = resetToken.getUser();

        // Hash and overwrite password
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        // Invalidate token immediately
        resetToken.setUsed(true);
        tokenRepository.save(resetToken);
    }
}
