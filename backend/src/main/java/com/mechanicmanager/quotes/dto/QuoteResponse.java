package com.mechanicmanager.quotes.dto;

import com.mechanicmanager.quotes.model.QuoteStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record QuoteResponse(
        Long id,
        Long number,
        Long customerId,
        String customerName,
        Long vehicleId,
        String vehicleLicensePlate,
        String vehicleDescription,
        LocalDate validUntil,
        QuoteStatus status,
        BigDecimal total,
        String notes,
        List<QuoteItemResponse> items,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
