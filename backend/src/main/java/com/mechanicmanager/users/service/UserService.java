package com.mechanicmanager.users.service;

import com.mechanicmanager.users.dto.CreateUserRequest;
import com.mechanicmanager.users.dto.UpdateUserRequest;
import com.mechanicmanager.users.dto.UpdateUserStatusRequest;
import com.mechanicmanager.users.dto.UserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {
    
    private final ListUsersUseCase listUsersUseCase;
    private final GetUserUseCase getUserUseCase;
    private final CreateUserUseCase createUserUseCase;
    private final UpdateUserUseCase updateUserUseCase;
    private final UpdateUserStatusUseCase updateUserStatusUseCase;
    
    public List<UserResponse> findAll() {
        return listUsersUseCase.execute();
    }
    
    public UserResponse findById(Long id) {
        return getUserUseCase.execute(id);
    }
    
    public UserResponse create(CreateUserRequest request) {
        return createUserUseCase.execute(request);
    }
    
    public UserResponse update(Long id, UpdateUserRequest request) {
        return updateUserUseCase.execute(id, request);
    }
    
    public UserResponse updateStatus(Long id, UpdateUserStatusRequest request, Long currentUserId) {
        return updateUserStatusUseCase.execute(id, request, currentUserId);
    }
}
