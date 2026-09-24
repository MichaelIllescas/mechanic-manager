package com.mechanicmanager.shared.search;

import java.util.Locale;

/**
 * Arma el patrón de una búsqueda "contiene" para {@code LIKE}.
 *
 * <p>En {@code LIKE}, {@code %} y {@code _} son comodines. Si el texto que
 * escribió el usuario los conserva, buscar {@code %} devuelve todo en vez de lo
 * que contiene ese símbolo. Se escapan para que el texto se busque literal.
 *
 * <p>No es un tema de inyección: los valores ya viajan parametrizados.
 */
public final class LikePattern {

    /** Carácter de escape; se pasa como tercer argumento de {@code criteriaBuilder.like}. */
    public static final char ESCAPE = '!';

    private LikePattern() {
    }

    /** Patrón que coincide con cualquier texto que contenga {@code text}, sin distinguir mayúsculas. */
    public static String contains(String text) {
        String escaped = text.toLowerCase(Locale.ROOT)
                .replace("!", "!!")
                .replace("%", "!%")
                .replace("_", "!_");

        return "%" + escaped + "%";
    }
}
