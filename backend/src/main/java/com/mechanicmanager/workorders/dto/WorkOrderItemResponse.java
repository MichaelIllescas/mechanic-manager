package com.mechanicmanager.workorders.dto;

import java.math.BigDecimal;

public record WorkOrderItemResponse(
        Long id,
        Long serviceTypeId,
        Long supplyId,
        String description,
        BigDecimal quantity,
        BigDecimal unitPrice,
        BigDecimal subtotal
) {
}
