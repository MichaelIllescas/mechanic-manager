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
        String customerDocumentNumber,
        String customerPhone,
        Long vehicleId,
        String vehicleLicensePlate,
        String vehicleDescription,
        LocalDate validUntil,
        QuoteStatus status,
        BigDecimal total,
        String notes,
        boolean hasWorkOrder,
        List<QuoteItemResponse> items,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
