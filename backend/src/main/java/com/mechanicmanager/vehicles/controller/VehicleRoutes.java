package com.mechanicmanager.vehicles.controller;

import com.mechanicmanager.shared.config.ApiRoutes;

public final class VehicleRoutes {

    public static final String BASE = ApiRoutes.API + "/vehicles";
    public static final String BY_ID = "/{id}";

    private VehicleRoutes() {
    }
}
