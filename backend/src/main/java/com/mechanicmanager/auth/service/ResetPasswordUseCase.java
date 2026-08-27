package com.mechanicmanager.auth.service;

import com.mechanicmanager.auth.dto.ResetPasswordRequest;
import com.mechanicmanager.auth.exception.InvalidPasswordResetTokenException;
import com.mechanicmanager.auth.model.PasswordResetToken;
import com.mechanicmanager.auth.repository.PasswordResetTokenRepository;
import com.mechanicmanager.users.model.User;
import com.mechanicmanager.users.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class ResetPasswordUseCase {
    
    private final PasswordResetTokenRepository tokenRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    
    @Transactional
    public void execute(ResetPasswordRequest request) {
        LocalDateTime now = LocalDateTime.now();
        
        PasswordResetToken token = tokenRepository
                .findByTokenHash(PasswordResetTokens.hash(request.token()))
                .orElseThrow(InvalidPasswordResetTokenException::new);
        
        if (!token.isUsable(now)) {
            throw new InvalidPasswordResetTokenException();
        }
        
        User user = token.getUser();
        if (!user.isEnabled()) {
            throw new InvalidPasswordResetTokenException();
        }
        
        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        user.setMustChangePassword(false);
        userRepository.save(user);
        
        // Un token sirve una sola vez: quien reabra el enlace ya no puede usarlo.
        token.markUsed(now);
        tokenRepository.save(token);
        
        log.info("Contrasena restablecida. userId={}", user.getId());
    }
}
