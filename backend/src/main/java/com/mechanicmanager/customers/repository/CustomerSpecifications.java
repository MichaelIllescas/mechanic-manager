package com.mechanicmanager.customers.repository;

import com.mechanicmanager.customers.model.Customer;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.Locale;

public final class CustomerSpecifications {

    private CustomerSpecifications() {
    }

    public static Specification<Customer> belongsToWorkshopAndMatches(
            Long workshopId,
            String search
    ) {
        return (root, query, criteriaBuilder) -> {
            String pattern = "%" + search.toLowerCase(Locale.ROOT) + "%";

            Predicate belongsToWorkshop = criteriaBuilder.equal(
                    root.get("workshop").get("id"),
                    workshopId
            );

            Expression<String> fullName = criteriaBuilder.concat(
                    criteriaBuilder.concat(root.get("firstName"), " "),
                    root.get("lastName")
            );

            Predicate matchesSearch = criteriaBuilder.or(
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("firstName")), pattern),
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("lastName")), pattern),
                    criteriaBuilder.like(criteriaBuilder.lower(fullName), pattern),
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("documentNumber")), pattern),
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("phone")), pattern),
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("email")), pattern)
            );

            return criteriaBuilder.and(belongsToWorkshop, matchesSearch);
        };
    }
}
