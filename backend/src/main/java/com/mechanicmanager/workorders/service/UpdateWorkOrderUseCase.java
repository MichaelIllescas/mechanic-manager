package com.mechanicmanager.workorders.service;

import com.mechanicmanager.shared.exception.ResourceNotFoundException;
import com.mechanicmanager.workorders.dto.UpdateWorkOrderRequest;
import com.mechanicmanager.workorders.dto.WorkOrderResponse;
import com.mechanicmanager.workorders.mapper.WorkOrderMapper;
import com.mechanicmanager.workorders.model.WorkOrder;
import com.mechanicmanager.workorders.model.WorkOrderItem;
import com.mechanicmanager.workorders.repository.WorkOrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UpdateWorkOrderUseCase {

    private final WorkOrderRepository workOrderRepository;
    private final WorkOrderItemFactory workOrderItemFactory;
    private final WorkOrderMapper workOrderMapper;

    @Transactional
    public WorkOrderResponse execute(
            Long userId,
            Long workOrderId,
            UpdateWorkOrderRequest request
    ) {
        WorkOrder workOrder = workOrderRepository.findByIdAndUserId(workOrderId, userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No se encontró la orden de trabajo indicada"
                ));

        List<WorkOrderItem> items = workOrderItemFactory.createAll(userId, request.items());

        workOrder.setPerformedAt(request.performedAt());
        workOrder.setMileage(request.mileage());
        workOrder.setNotes(trimToNull(request.notes()));
        workOrder.replaceItems(items);

        return workOrderMapper.toResponse(workOrderRepository.save(workOrder));
    }

    private String trimToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
