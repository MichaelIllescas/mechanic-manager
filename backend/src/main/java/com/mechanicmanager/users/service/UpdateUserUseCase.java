package com.mechanicmanager.users.service;

import com.mechanicmanager.shared.exception.ResourceNotFoundException;
import com.mechanicmanager.users.dto.UpdateUserRequest;
import com.mechanicmanager.users.dto.UserResponse;
import com.mechanicmanager.users.exception.EmailAlreadyExistsException;
import com.mechanicmanager.users.mapper.UserMapper;
import com.mechanicmanager.users.model.Role;
import com.mechanicmanager.users.model.User;
import com.mechanicmanager.users.repository.UserRepository;
import com.mechanicmanager.workshops.service.WorkshopService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UpdateUserUseCase {
    
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final WorkshopService workshopService;
    
    @Transactional
    public UserResponse execute(Long id, UpdateUserRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No se encontró el usuario con id " + id));
        
        String email = request.email().trim().toLowerCase();
        
        userRepository.findByEmail(email)
                .filter(other -> !other.getId().equals(id))
                .ifPresent(other -> {
                    throw new EmailAlreadyExistsException(email);
                });
        
        user.setName(request.name().trim());
        user.setEmail(email);
        user.setRole(request.role());
        
        User savedUser = userRepository.save(user);
        
        // Una cuenta promovida a USER necesita su taller; si ya lo tenía,
        // registerFor no hace nada. El taller de una cuenta que pasa a
        // SYSTEM_ADMIN se conserva, para no perder los datos si vuelve a USER.
        if (savedUser.getRole() == Role.USER) {
            workshopService.registerFor(savedUser);
        }
        
        return userMapper.toResponse(savedUser);
    }
}
