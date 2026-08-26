package com.mechanicmanager.servicetypes.controller;

import com.mechanicmanager.shared.config.ApiRoutes;

public final class ServiceTypeRoutes {
    
    public static final String BASE = ApiRoutes.API + "/service-types";
    public static final String BY_ID = "/{id}";
    public static final String STATUS = "/{id}/status";
    
    private ServiceTypeRoutes() {
    }
}
