package com.mechanicmanager.customers.service;

import com.mechanicmanager.customers.dto.CreateCustomerRequest;
import com.mechanicmanager.customers.dto.CustomerResponse;
import com.mechanicmanager.customers.dto.UpdateCustomerRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomerService {

    private final CreateCustomerUseCase createCustomerUseCase;
    private final ListCustomersUseCase listCustomersUseCase;
    private final SearchCustomersUseCase searchCustomersUseCase;
    private final UpdateCustomerUseCase updateCustomerUseCase;

    public CustomerResponse create(Long userId, CreateCustomerRequest request) {
        return createCustomerUseCase.execute(userId, request);
    }

    public List<CustomerResponse> findAll(Long userId, String search) {
        if (search == null || search.isBlank()) {
            return listCustomersUseCase.execute(userId);
        }
        return searchCustomersUseCase.execute(userId, search);
    }

    public CustomerResponse update(
            Long userId,
            Long customerId,
            UpdateCustomerRequest request
    ) {
        return updateCustomerUseCase.execute(userId, customerId, request);
    }
}
