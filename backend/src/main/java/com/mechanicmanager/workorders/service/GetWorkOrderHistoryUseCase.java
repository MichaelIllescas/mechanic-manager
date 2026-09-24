package com.mechanicmanager.workorders.service;

import com.mechanicmanager.workorders.dto.WorkOrderResponse;
import com.mechanicmanager.workorders.exception.InvalidWorkOrderException;
import com.mechanicmanager.workorders.mapper.WorkOrderMapper;
import com.mechanicmanager.workorders.model.WorkOrder;
import com.mechanicmanager.workorders.repository.WorkOrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GetWorkOrderHistoryUseCase {

    private final WorkOrderRepository workOrderRepository;
    private final WorkOrderMapper workOrderMapper;

    @Transactional(readOnly = true)
    public List<WorkOrderResponse> execute(Long userId, Long customerId, Long vehicleId) {
        if ((customerId == null) == (vehicleId == null)) {
            throw new InvalidWorkOrderException(
                    "Debe indicarse un cliente o un vehículo, pero no ambos"
            );
        }

        List<WorkOrder> workOrders = vehicleId != null
                ? workOrderRepository.findByUserIdAndVehicleIdOrderByPerformedAtDescIdDesc(userId, vehicleId)
                : workOrderRepository.findByUserIdAndVehicleCustomerIdOrderByPerformedAtDescIdDesc(userId, customerId);

        return workOrders.stream()
                .map(workOrderMapper::toResponse)
                .toList();
    }
}
