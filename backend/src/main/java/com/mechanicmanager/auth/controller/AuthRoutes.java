package com.mechanicmanager.auth.controller;

import com.mechanicmanager.shared.config.ApiRoutes;

public final class AuthRoutes {
    
    public static final String BASE = ApiRoutes.API + "/auth";
    public static final String LOGIN = "/login";
    public static final String FORGOT_PASSWORD = "/forgot-password";
    public static final String RESET_PASSWORD = "/reset-password";
    public static final String CHANGE_PASSWORD = "/change-password";
    
    private AuthRoutes() {
    }
}
