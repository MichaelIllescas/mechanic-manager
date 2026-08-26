package com.mechanicmanager.users.controller;

import com.mechanicmanager.shared.config.ApiRoutes;

public final class UserRoutes {
    
    public static final String BASE = ApiRoutes.ADMIN + "/users";
    public static final String BY_ID = "/{id}";
    public static final String STATUS = "/{id}/status";
    
    private UserRoutes() {
    }
}
