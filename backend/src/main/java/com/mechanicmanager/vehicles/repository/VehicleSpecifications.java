package com.mechanicmanager.vehicles.repository;

import com.mechanicmanager.vehicles.model.Vehicle;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.Locale;

public final class VehicleSpecifications {

    private VehicleSpecifications() {
    }

    public static Specification<Vehicle> belongsToUserAndLicensePlateMatches(
            Long userId,
            String search
    ) {
        return (root, query, criteriaBuilder) -> {
            String pattern = "%" + search.toLowerCase(Locale.ROOT) + "%";

            Predicate belongsToUser = criteriaBuilder.equal(
                    root.get("user").get("id"),
                    userId
            );

            Predicate licensePlateMatches = criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("licensePlate")),
                    pattern
            );

            return criteriaBuilder.and(belongsToUser, licensePlateMatches);
        };
    }
}
