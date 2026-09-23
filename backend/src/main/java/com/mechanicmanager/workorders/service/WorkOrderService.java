package com.mechanicmanager.workorders.service;

import com.mechanicmanager.workorders.dto.CreateWorkOrderRequest;
import com.mechanicmanager.workorders.dto.UpdateWorkOrderRequest;
import com.mechanicmanager.workorders.dto.UpdateWorkOrderStatusRequest;
import com.mechanicmanager.workorders.dto.WorkOrderPageResponse;
import com.mechanicmanager.workorders.dto.WorkOrderResponse;
import com.mechanicmanager.workorders.model.WorkOrderStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class WorkOrderService {

    private final CreateWorkOrderUseCase createWorkOrderUseCase;
    private final GetWorkOrderUseCase getWorkOrderUseCase;
    private final SearchWorkOrdersUseCase searchWorkOrdersUseCase;
    private final UpdateWorkOrderUseCase updateWorkOrderUseCase;
    private final UpdateWorkOrderStatusUseCase updateWorkOrderStatusUseCase;
    private final DeleteWorkOrderUseCase deleteWorkOrderUseCase;

    public WorkOrderResponse create(Long userId, CreateWorkOrderRequest request) {
        return createWorkOrderUseCase.execute(userId, request);
    }

    public WorkOrderResponse findById(Long userId, Long workOrderId) {
        return getWorkOrderUseCase.execute(userId, workOrderId);
    }

    public WorkOrderPageResponse search(
            Long userId,
            String search,
            WorkOrderStatus status,
            Long vehicleId,
            LocalDate from,
            LocalDate to,
            int page,
            int size
    ) {
        return searchWorkOrdersUseCase.execute(
                userId,
                search,
                status,
                vehicleId,
                from,
                to,
                page,
                size
        );
    }

    public WorkOrderResponse update(
            Long userId,
            Long workOrderId,
            UpdateWorkOrderRequest request
    ) {
        return updateWorkOrderUseCase.execute(userId, workOrderId, request);
    }

    public WorkOrderResponse updateStatus(
            Long userId,
            Long workOrderId,
            UpdateWorkOrderStatusRequest request
    ) {
        return updateWorkOrderStatusUseCase.execute(userId, workOrderId, request);
    }

    public void delete(Long userId, Long workOrderId) {
        deleteWorkOrderUseCase.execute(userId, workOrderId);
    }
}
