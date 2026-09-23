package com.mechanicmanager.workorders.mapper;

import com.mechanicmanager.workorders.dto.WorkOrderItemResponse;
import com.mechanicmanager.workorders.dto.WorkOrderResponse;
import com.mechanicmanager.workorders.dto.WorkOrderSummaryResponse;
import com.mechanicmanager.workorders.model.WorkOrder;
import com.mechanicmanager.workorders.model.WorkOrderItem;
import org.springframework.stereotype.Component;

@Component
public class WorkOrderMapper {

    public WorkOrderResponse toResponse(WorkOrder workOrder) {
        return new WorkOrderResponse(
                workOrder.getId(),
                workOrder.getVehicle().getId(),
                workOrder.getVehicle().getLicensePlate(),
                vehicleDescription(workOrder),
                workOrder.getVehicle().getCustomer().getId(),
                customerName(workOrder),
                workOrder.getVehicle().getCustomer().getDocumentNumber(),
                workOrder.getVehicle().getCustomer().getPhone(),
                workOrder.getAppointment() == null
                        ? null
                        : workOrder.getAppointment().getId(),
                workOrder.getQuote() == null
                        ? null
                        : workOrder.getQuote().getId(),
                workOrder.getQuote() == null
                        ? null
                        : workOrder.getQuote().getNumber(),
                workOrder.getPerformedAt(),
                workOrder.getMileage(),
                workOrder.getStatus(),
                workOrder.getTotal(),
                workOrder.getNotes(),
                workOrder.getItems().stream()
                        .map(this::toItemResponse)
                        .toList(),
                workOrder.getCreatedAt(),
                workOrder.getUpdatedAt()
        );
    }

    public WorkOrderSummaryResponse toSummaryResponse(WorkOrder workOrder) {
        return new WorkOrderSummaryResponse(
                workOrder.getId(),
                workOrder.getVehicle().getId(),
                workOrder.getVehicle().getLicensePlate(),
                workOrder.getVehicle().getCustomer().getId(),
                customerName(workOrder),
                workOrder.getPerformedAt(),
                workOrder.getStatus(),
                workOrder.getTotal()
        );
    }

    private WorkOrderItemResponse toItemResponse(WorkOrderItem item) {
        return new WorkOrderItemResponse(
                item.getId(),
                item.getServiceType() == null
                        ? null
                        : item.getServiceType().getId(),
                item.getSupply() == null
                        ? null
                        : item.getSupply().getId(),
                item.getDescription(),
                item.getQuantity(),
                item.getUnitPrice(),
                item.getSubtotal()
        );
    }

    private String customerName(WorkOrder workOrder) {
        return workOrder.getVehicle().getCustomer().getFirstName()
                + " "
                + workOrder.getVehicle().getCustomer().getLastName();
    }

    private String vehicleDescription(WorkOrder workOrder) {
        return workOrder.getVehicle().getBrand()
                + " "
                + workOrder.getVehicle().getModel();
    }
}
