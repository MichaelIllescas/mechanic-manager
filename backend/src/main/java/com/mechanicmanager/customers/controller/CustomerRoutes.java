package com.mechanicmanager.customers.controller;

import com.mechanicmanager.shared.config.ApiRoutes;

public final class CustomerRoutes {

    public static final String BASE = ApiRoutes.API + "/customers";
    public static final String BY_ID = "/{id}";

    private CustomerRoutes() {
    }
}
