package com.mechanicmanager.users.mapper;

import com.mechanicmanager.users.dto.UserResponse;
import com.mechanicmanager.users.model.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {
    
    public UserResponse toResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole(),
                user.getStatus(),
                user.isMustChangePassword(),
                user.getCreatedAt()
        );
    }
}
