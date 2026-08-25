package com.mechanicmanager.users.service;

import com.mechanicmanager.shared.exception.ResourceNotFoundException;
import com.mechanicmanager.users.dto.UserResponse;
import com.mechanicmanager.users.mapper.UserMapper;
import com.mechanicmanager.users.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GetUserUseCase {
    
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    
    @Transactional(readOnly = true)
    public UserResponse execute(Long id) {
        return userRepository.findById(id)
                .map(userMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No se encontró el usuario con id " + id));
    }
}
