package com.mechanicmanager.workorders.dto;

import com.mechanicmanager.workorders.model.WorkOrderStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record WorkOrderResponse(
        Long id,
        Long vehicleId,
        String vehicleLicensePlate,
        String vehicleDescription,
        Long customerId,
        String customerName,
        String customerDocumentNumber,
        String customerPhone,
        Long appointmentId,
        Long quoteId,
        Long quoteNumber,
        LocalDate performedAt,
        Integer mileage,
        WorkOrderStatus status,
        BigDecimal total,
        String notes,
        List<WorkOrderItemResponse> items,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
