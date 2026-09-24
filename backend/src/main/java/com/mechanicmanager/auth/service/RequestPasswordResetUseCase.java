package com.mechanicmanager.auth.service;

import com.mechanicmanager.auth.dto.ForgotPasswordRequest;
import com.mechanicmanager.auth.exception.TooManyAttemptsException;
import com.mechanicmanager.auth.model.PasswordResetToken;
import com.mechanicmanager.auth.repository.PasswordResetTokenRepository;
import com.mechanicmanager.shared.email.EmailMessage;
import com.mechanicmanager.shared.email.EmailSender;
import com.mechanicmanager.shared.security.AttemptLimiter;
import com.mechanicmanager.users.model.User;
import com.mechanicmanager.users.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
public class RequestPasswordResetUseCase {
    
    private static final String TEMPLATE = "password-reset";
    private static final String SUBJECT = "Recuperar tu contraseña";

    /** Cada pedido manda un email: se cuentan todos, exista o no la cuenta, para no revelar cuáles existen. */
    static final int MAX_REQUESTS_PER_EMAIL = 3;
    static final int MAX_REQUESTS_PER_IP = 10;
    
    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final EmailSender emailSender;
    private final AttemptLimiter attemptLimiter;
    private final Duration expiration;
    private final String resetUrlTemplate;
    
    public RequestPasswordResetUseCase(
            UserRepository userRepository,
            PasswordResetTokenRepository tokenRepository,
            EmailSender emailSender,
            AttemptLimiter attemptLimiter,
            @Value("${security.password-reset.expiration-minutes}") long expirationMinutes,
            @Value("${app.frontend.base-url}") String frontendBaseUrl,
            @Value("${app.frontend.reset-password-path}") String resetPasswordPath
    ) {
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
        this.emailSender = emailSender;
        this.attemptLimiter = attemptLimiter;
        this.expiration = Duration.ofMinutes(expirationMinutes);
        this.resetUrlTemplate = frontendBaseUrl + resetPasswordPath;
    }
    
    @Transactional
    public void execute(ForgotPasswordRequest request, String clientIp) {
        String email = request.email().trim().toLowerCase();
        String emailKey = "forgot:email:" + email;
        String ipKey = "forgot:ip:" + clientIp;
        
        if (attemptLimiter.isBlocked(emailKey, MAX_REQUESTS_PER_EMAIL)
                || attemptLimiter.isBlocked(ipKey, MAX_REQUESTS_PER_IP)) {
            throw new TooManyAttemptsException();
        }
        attemptLimiter.record(emailKey);
        attemptLimiter.record(ipKey);
        
        Optional<User> found = userRepository.findByEmail(email);
        
        // Respuesta identica exista o no la cuenta: el endpoint es publico y no
        // debe permitir averiguar que emails estan registrados.
        if (found.isEmpty() || !found.get().isEnabled()) {
            log.info("Pedido de recuperacion para una cuenta inexistente o deshabilitada");
            return;
        }
        
        User user = found.get();
        LocalDateTime now = LocalDateTime.now();
        
        tokenRepository.invalidatePendingTokens(user.getId(), now);
        
        String token = PasswordResetTokens.generate();
        tokenRepository.save(PasswordResetToken.builder()
                .user(user)
                .tokenHash(PasswordResetTokens.hash(token))
                .expiresAt(now.plus(expiration))
                .build());
        
        emailSender.send(new EmailMessage(
                user.getEmail(),
                SUBJECT,
                TEMPLATE,
                Map.of(
                        "userName", user.getName(),
                        "resetUrl", buildResetUrl(token),
                        "expirationMinutes", expiration.toMinutes()
                )
        ));
        
        log.info("Enlace de recuperacion enviado. userId={}", user.getId());
    }
    
    private String buildResetUrl(String token) {
        return UriComponentsBuilder.fromUriString(resetUrlTemplate)
                .queryParam("token", token)
                .build()
                .toUriString();
    }
}
