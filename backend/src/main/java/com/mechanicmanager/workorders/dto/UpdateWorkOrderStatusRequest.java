package com.mechanicmanager.workorders.dto;

import com.mechanicmanager.workorders.model.WorkOrderStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateWorkOrderStatusRequest(

        @NotNull(message = "El estado es obligatorio")
        WorkOrderStatus status
) {
}
