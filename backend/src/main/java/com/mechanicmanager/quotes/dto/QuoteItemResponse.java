package com.mechanicmanager.quotes.dto;

import java.math.BigDecimal;

public record QuoteItemResponse(
        Long id,
        Long serviceTypeId,
        Long supplyId,
        String description,
        BigDecimal quantity,
        BigDecimal unitPrice,
        BigDecimal subtotal
) {
}
