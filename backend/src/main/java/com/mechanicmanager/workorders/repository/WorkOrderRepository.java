package com.mechanicmanager.workorders.repository;

import com.mechanicmanager.workorders.model.WorkOrder;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface WorkOrderRepository extends
        JpaRepository<WorkOrder, Long>,
        JpaSpecificationExecutor<WorkOrder> {

    @EntityGraph(attributePaths = {
            "vehicle",
            "vehicle.customer",
            "appointment",
            "quote",
            "items",
            "items.serviceType",
            "items.supply"
    })
    Optional<WorkOrder> findByIdAndUserId(Long id, Long userId);

    Optional<WorkOrder> findByAppointmentIdAndUserId(Long appointmentId, Long userId);

    Optional<WorkOrder> findByQuoteIdAndUserId(Long quoteId, Long userId);
}
