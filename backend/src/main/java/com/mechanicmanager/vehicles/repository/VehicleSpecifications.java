package com.mechanicmanager.vehicles.repository;

import com.mechanicmanager.vehicles.model.Vehicle;
import com.mechanicmanager.shared.search.LikePattern;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

public final class VehicleSpecifications {

    private VehicleSpecifications() {
    }

    public static Specification<Vehicle> belongsToUserAndLicensePlateMatches(
            Long userId,
            String search
    ) {
        return (root, query, criteriaBuilder) -> {
            String pattern = LikePattern.contains(search);

            Predicate belongsToUser = criteriaBuilder.equal(
                    root.get("user").get("id"),
                    userId
            );

            Predicate licensePlateMatches = criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("licensePlate")),
                    pattern, LikePattern.ESCAPE
            );

            return criteriaBuilder.and(belongsToUser, licensePlateMatches);
        };
    }
}
