package com.mechanicmanager.users.service;

import com.mechanicmanager.users.dto.UserResponse;
import com.mechanicmanager.users.mapper.UserMapper;
import com.mechanicmanager.users.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ListUsersUseCase {
    
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    
    @Transactional(readOnly = true)
    public List<UserResponse> execute() {
        return userRepository.findAll().stream()
                .map(userMapper::toResponse)
                .toList();
    }
}
