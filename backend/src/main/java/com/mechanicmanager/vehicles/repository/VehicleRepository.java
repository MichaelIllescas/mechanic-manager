package com.mechanicmanager.vehicles.repository;

import com.mechanicmanager.vehicles.model.Vehicle;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface VehicleRepository extends
        JpaRepository<Vehicle, Long>,
        JpaSpecificationExecutor<Vehicle> {

    @EntityGraph(attributePaths = "customer")
    List<Vehicle> findAllByWorkshop_IdOrderByLicensePlateAsc(Long workshopId);

    @EntityGraph(attributePaths = "customer")
    Optional<Vehicle> findByIdAndWorkshop_Id(Long id, Long workshopId);

    boolean existsByWorkshop_IdAndLicensePlateIgnoreCase(
            Long workshopId,
            String licensePlate
    );

    boolean existsByWorkshop_IdAndLicensePlateIgnoreCaseAndIdNot(
            Long workshopId,
            String licensePlate,
            Long id
    );
}
