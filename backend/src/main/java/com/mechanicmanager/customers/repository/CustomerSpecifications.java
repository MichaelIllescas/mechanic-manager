package com.mechanicmanager.customers.repository;

import com.mechanicmanager.customers.model.Customer;
import com.mechanicmanager.shared.search.LikePattern;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

public final class CustomerSpecifications {

    private CustomerSpecifications() {
    }

    public static Specification<Customer> belongsToUserAndMatches(
            Long userId,
            String search
    ) {
        return (root, query, criteriaBuilder) -> {
            String pattern = LikePattern.contains(search);

            Predicate belongsToUser = criteriaBuilder.equal(
                    root.get("user").get("id"),
                    userId
            );

            Expression<String> fullName = criteriaBuilder.concat(
                    criteriaBuilder.concat(root.get("firstName"), " "),
                    root.get("lastName")
            );

            Predicate matchesSearch = criteriaBuilder.or(
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("firstName")), pattern, LikePattern.ESCAPE),
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("lastName")), pattern, LikePattern.ESCAPE),
                    criteriaBuilder.like(criteriaBuilder.lower(fullName), pattern, LikePattern.ESCAPE),
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("documentNumber")), pattern, LikePattern.ESCAPE),
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("phone")), pattern, LikePattern.ESCAPE),
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("email")), pattern, LikePattern.ESCAPE)
            );

            return criteriaBuilder.and(belongsToUser, matchesSearch);
        };
    }
}
