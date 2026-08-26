package com.mechanicmanager.supplies.controller;

import com.mechanicmanager.shared.config.ApiRoutes;

public final class SupplyRoutes {
    
    public static final String BASE = ApiRoutes.API + "/supplies";
    public static final String BY_ID = "/{id}";
    public static final String STATUS = "/{id}/status";
    
    private SupplyRoutes() {
    }
}
