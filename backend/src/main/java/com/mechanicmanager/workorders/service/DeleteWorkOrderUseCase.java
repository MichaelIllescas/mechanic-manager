package com.mechanicmanager.workorders.service;

import com.mechanicmanager.shared.exception.ResourceNotFoundException;
import com.mechanicmanager.workorders.model.WorkOrder;
import com.mechanicmanager.workorders.repository.WorkOrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DeleteWorkOrderUseCase {

    private final WorkOrderRepository workOrderRepository;

    @Transactional
    public void execute(Long userId, Long workOrderId) {
        WorkOrder workOrder = workOrderRepository.findByIdAndUserId(workOrderId, userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No se encontró el trabajo realizado indicado"
                ));

        workOrderRepository.delete(workOrder);
    }
}
