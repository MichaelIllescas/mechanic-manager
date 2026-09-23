package com.mechanicmanager.workorders.service;

import com.mechanicmanager.shared.exception.ResourceNotFoundException;
import com.mechanicmanager.workorders.dto.WorkOrderResponse;
import com.mechanicmanager.workorders.mapper.WorkOrderMapper;
import com.mechanicmanager.workorders.model.WorkOrder;
import com.mechanicmanager.workorders.repository.WorkOrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GetWorkOrderUseCase {

    private final WorkOrderRepository workOrderRepository;
    private final WorkOrderMapper workOrderMapper;

    @Transactional(readOnly = true)
    public WorkOrderResponse execute(Long userId, Long workOrderId) {
        WorkOrder workOrder = workOrderRepository.findByIdAndUserId(workOrderId, userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No se encontró la orden de trabajo indicada"
                ));

        return workOrderMapper.toResponse(workOrder);
    }
}
