package com.mechanicmanager.shared.validation;

import java.math.BigDecimal;

/**
 * Máximos que caben en las columnas de importes. Superarlos no da un error de
 * validación sino un rechazo de la base, así que se controlan antes de guardar.
 */
public final class MoneyLimits {

    /** Subtotales y totales: {@code DECIMAL(14, 2)}, o sea 12 enteros y 2 decimales. */
    public static final BigDecimal MAX_AMOUNT = new BigDecimal("999999999999.99");

    private MoneyLimits() {
    }

    public static boolean exceedsMaxAmount(BigDecimal amount) {
        return amount.abs().compareTo(MAX_AMOUNT) > 0;
    }
}
