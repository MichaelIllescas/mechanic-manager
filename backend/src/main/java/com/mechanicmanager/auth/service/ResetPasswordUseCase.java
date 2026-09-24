package com.mechanicmanager.auth.service;

import com.mechanicmanager.auth.dto.ResetPasswordRequest;
import com.mechanicmanager.auth.exception.InvalidPasswordResetTokenException;
import com.mechanicmanager.auth.exception.TooManyAttemptsException;
import com.mechanicmanager.auth.model.PasswordResetToken;
import com.mechanicmanager.auth.repository.PasswordResetTokenRepository;
import com.mechanicmanager.shared.security.AttemptLimiter;
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
    private final AttemptLimiter attemptLimiter;

    /** Un token de 256 bits no se adivina; el límite corta el sondeo automatizado, no protege el token. */
    static final int MAX_FAILURES_PER_IP = 10;
    
    @Transactional
    public void execute(ResetPasswordRequest request, String clientIp) {
        String ipKey = "reset:ip:" + clientIp;
        
        if (attemptLimiter.isBlocked(ipKey, MAX_FAILURES_PER_IP)) {
            throw new TooManyAttemptsException();
        }
        
        try {
            reset(request);
        } catch (InvalidPasswordResetTokenException e) {
            attemptLimiter.record(ipKey);
            throw e;
        }
    }
    
    private void reset(ResetPasswordRequest request) {
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
