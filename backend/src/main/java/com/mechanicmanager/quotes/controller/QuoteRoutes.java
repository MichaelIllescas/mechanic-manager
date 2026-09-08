package com.mechanicmanager.quotes.controller;

import com.mechanicmanager.shared.config.ApiRoutes;

public final class QuoteRoutes {

    public static final String BASE = ApiRoutes.API + "/quotes";
    public static final String BY_ID = "/{id}";
    public static final String STATUS = BY_ID + "/status";

    private QuoteRoutes() {
    }
}
