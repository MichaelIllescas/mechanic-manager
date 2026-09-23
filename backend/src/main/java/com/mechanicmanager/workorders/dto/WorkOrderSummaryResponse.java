package com.mechanicmanager.workorders.dto;

import com.mechanicmanager.workorders.model.WorkOrderStatus;

import java.math.BigDecimal;
import java.time.LocalDate;

public record WorkOrderSummaryResponse(
        Long id,
        Long vehicleId,
        String vehicleLicensePlate,
        Long customerId,
        String customerName,
        LocalDate performedAt,
        WorkOrderStatus status,
        BigDecimal total
) {
}
