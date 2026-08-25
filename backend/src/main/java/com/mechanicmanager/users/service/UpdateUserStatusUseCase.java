package com.mechanicmanager.users.service;

import com.mechanicmanager.shared.exception.AccessDeniedAppException;
import com.mechanicmanager.shared.exception.ResourceNotFoundException;
import com.mechanicmanager.users.dto.UpdateUserStatusRequest;
import com.mechanicmanager.users.dto.UserResponse;
import com.mechanicmanager.users.mapper.UserMapper;
import com.mechanicmanager.users.model.User;
import com.mechanicmanager.users.model.UserStatus;
import com.mechanicmanager.users.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UpdateUserStatusUseCase {
    
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    
    @Transactional
    public UserResponse execute(Long id, UpdateUserStatusRequest request, Long currentUserId) {
        if (request.status() == UserStatus.DISABLED && id.equals(currentUserId)) {
            throw new AccessDeniedAppException("No podés deshabilitar tu propia cuenta");
        }
        
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No se encontró el usuario con id " + id));
        
        user.setStatus(request.status());
        
        return userMapper.toResponse(userRepository.save(user));
    }
}
