package com.mechanicmanager.auth.service;

import com.mechanicmanager.auth.dto.ChangePasswordRequest;
import com.mechanicmanager.auth.exception.InvalidCurrentPasswordException;
import com.mechanicmanager.auth.exception.PasswordNotChangedException;
import com.mechanicmanager.shared.exception.ResourceNotFoundException;
import com.mechanicmanager.users.model.User;
import com.mechanicmanager.users.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChangePasswordUseCase {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    
    @Transactional
    public void execute(Long userId, ChangePasswordRequest request) {
        // Se relee de la base: el hash del principal viene del momento del login
        // y podria estar desactualizado.
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontro el usuario " + userId));
        
        if (!passwordEncoder.matches(request.currentPassword(), user.getPasswordHash())) {
            log.warn("Cambio de contrasena rechazado: contrasena actual incorrecta. userId={}", userId);
            throw new InvalidCurrentPasswordException();
        }
        
        // Con una contraseña temporal, "cambiarla" por la misma dejaría la
        // cuenta igual de expuesta y con el flag apagado.
        if (request.newPassword().equals(request.currentPassword())) {
            throw new PasswordNotChangedException();
        }
        
        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        user.setMustChangePassword(false);
        userRepository.save(user);
        
        log.info("Contrasena cambiada por el propio usuario. userId={}", userId);
    }
}
