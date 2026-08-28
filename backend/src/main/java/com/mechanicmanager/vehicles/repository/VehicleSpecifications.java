package com.mechanicmanager.vehicles.repository;

import com.mechanicmanager.vehicles.model.Vehicle;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.Locale;

public final class VehicleSpecifications {

    private VehicleSpecifications() {
    }

    public static Specification<Vehicle> belongsToWorkshopAndLicensePlateMatches(
            Long workshopId,
            String search
    ) {
        return (root, query, criteriaBuilder) -> {
            String pattern = "%" + search.toLowerCase(Locale.ROOT) + "%";

            Predicate belongsToWorkshop = criteriaBuilder.equal(
                    root.get("workshop").get("id"),
                    workshopId
            );

            Predicate licensePlateMatches = criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("licensePlate")),
                    pattern
            );

            return criteriaBuilder.and(belongsToWorkshop, licensePlateMatches);
        };
    }
}
