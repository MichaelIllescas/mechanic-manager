package com.mechanicmanager.shared.config;

/**
 * Raíz de la API. Cada módulo define su propia ruta a partir de acá,
 * para que versionar o renombrar el prefijo sea un cambio en un solo lugar.
 */
public final class ApiRoutes {
    
    public static final String API = "/api/v1";
    public static final String ADMIN = API + "/admin";
    
    private ApiRoutes() {
    }
}
