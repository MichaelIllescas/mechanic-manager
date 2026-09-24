package com.mechanicmanager.workorders.controller;

import com.mechanicmanager.shared.config.ApiRoutes;

public final class WorkOrderRoutes {

    public static final String BASE = ApiRoutes.API + "/work-orders";
    public static final String HISTORY = "/history";
    public static final String BY_ID = "/{id}";
    public static final String STATUS = BY_ID + "/status";

    private WorkOrderRoutes() {
    }
}
