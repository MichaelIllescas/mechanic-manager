package com.mechanicmanager.appointments.controller;

import com.mechanicmanager.shared.config.ApiRoutes;

public final class AppointmentRoutes {

    public static final String BASE = ApiRoutes.API + "/appointments";
    public static final String BY_ID = "/{id}";

    /**
     * Completar y cancelar son acciones sobre el turno y no el reemplazo de un
     * campo, así que tienen endpoint propio en lugar de un PATCH de estado:
     * cada transición documenta sus propios códigos de error.
     */
    public static final String COMPLETE = BY_ID + "/complete";
    public static final String CANCEL = BY_ID + "/cancel";

    private AppointmentRoutes() {
    }
}
