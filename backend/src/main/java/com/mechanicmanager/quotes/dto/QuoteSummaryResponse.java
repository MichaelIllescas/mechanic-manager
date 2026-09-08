package com.mechanicmanager.quotes.dto;

import com.mechanicmanager.quotes.model.QuoteStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record QuoteSummaryResponse(
        Long id,
        Long number,
        Long customerId,
        String customerName,
        Long vehicleId,
        String vehicleLicensePlate,
        LocalDate validUntil,
        QuoteStatus status,
        BigDecimal total,
        LocalDateTime createdAt
) {
}
