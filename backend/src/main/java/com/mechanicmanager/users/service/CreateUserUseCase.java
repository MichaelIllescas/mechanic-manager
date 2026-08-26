package com.mechanicmanager.users.service;

import com.mechanicmanager.users.dto.CreateUserRequest;
import com.mechanicmanager.users.dto.UserResponse;
import com.mechanicmanager.users.exception.EmailAlreadyExistsException;
import com.mechanicmanager.users.mapper.UserMapper;
import com.mechanicmanager.users.model.Role;
import com.mechanicmanager.users.model.User;
import com.mechanicmanager.users.model.UserStatus;
import com.mechanicmanager.users.repository.UserRepository;
import com.mechanicmanager.workshops.model.Workshop;
import com.mechanicmanager.workshops.service.WorkshopService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CreateUserUseCase {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    private final WorkshopService workshopService;
    
    
    @Transactional
    public UserResponse execute(CreateUserRequest request) {
        String email = request.email().trim().toLowerCase();
        
        if (userRepository.existsByEmail(email)) {
            throw new EmailAlreadyExistsException(email);
        }
        
        User user = User.builder()
                .name(request.name().trim())
                .email(email)
                .passwordHash(passwordEncoder.encode(request.password()))
                .role(request.role())
                .status(UserStatus.ENABLED)
                .mustChangePassword(true)
                .build();
        
        User savedUser = userRepository.save(user);
        
        // El taller pertenece a quien opera la aplicación; un SYSTEM_ADMIN
        // administra cuentas y no tiene taller propio.
        if (savedUser.getRole() == Role.USER) {
            workshopService.registerFor(savedUser);
        }
        
        return userMapper.toResponse(savedUser);
    }
}
