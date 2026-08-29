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
    List<Vehicle> findAllByUserIdOrderByLicensePlateAsc(Long userId);

    @EntityGraph(attributePaths = "customer")
    Optional<Vehicle> findByIdAndUserId(Long id, Long userId);

    boolean existsByUserIdAndLicensePlateIgnoreCase(
            Long userId,
            String licensePlate
    );

    boolean existsByUserIdAndLicensePlateIgnoreCaseAndIdNot(
            Long userId,
            String licensePlate,
            Long id
    );
}
