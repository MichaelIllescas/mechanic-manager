package com.mechanicmanager.customers.mapper;

import com.mechanicmanager.customers.dto.CustomerResponse;
import com.mechanicmanager.customers.model.Customer;
import org.springframework.stereotype.Component;

@Component
public class CustomerMapper {

    public CustomerResponse toResponse(Customer customer) {
        return new CustomerResponse(
                customer.getId(),
                customer.getFirstName(),
                customer.getLastName(),
                customer.getDocumentNumber(),
                customer.getPhone(),
                customer.getEmail(),
                customer.getNotes(),
                customer.getCreatedAt(),
                customer.getUpdatedAt()
        );
    }
}
