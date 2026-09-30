package com.mechanicmanager.shared.text;

public final class Texts {

    private Texts() {
    }

    /**
     * El front envía "" cuando el usuario vacía un campo; se guarda como null
     * para no mezclar nulos y strings vacíos en la base.
     */
    public static String trimToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
