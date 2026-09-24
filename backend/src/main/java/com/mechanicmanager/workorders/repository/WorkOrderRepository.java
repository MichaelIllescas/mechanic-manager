package com.mechanicmanager.workorders.repository;

import com.mechanicmanager.workorders.model.WorkOrder;
import com.mechanicmanager.workorders.model.WorkOrderStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.LocalDate;
import java.util.List;
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

    @EntityGraph(attributePaths = {
            "vehicle",
            "vehicle.customer",
            "quote",
            "items"
    })
    List<WorkOrder> findByUserIdAndVehicleIdOrderByPerformedAtDescIdDesc(Long userId, Long vehicleId);

    @EntityGraph(attributePaths = {
            "vehicle",
            "vehicle.customer",
            "quote",
            "items"
    })
    List<WorkOrder> findByUserIdAndVehicleCustomerIdOrderByPerformedAtDescIdDesc(Long userId, Long customerId);

    Optional<WorkOrder> findByAppointmentIdAndUserId(Long appointmentId, Long userId);

    Optional<WorkOrder> findByQuoteIdAndUserId(Long quoteId, Long userId);

    long countByUserIdAndStatusAndPerformedAtGreaterThanEqualAndPerformedAtLessThan(
            Long userId,
            WorkOrderStatus status,
            LocalDate from,
            LocalDate to
    );
}
