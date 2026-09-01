package com.mechanicmanager.customers.service;

import com.mechanicmanager.customers.dto.CustomerResponse;
import com.mechanicmanager.customers.dto.UpdateCustomerRequest;
import com.mechanicmanager.customers.mapper.CustomerMapper;
import com.mechanicmanager.customers.model.Customer;
import com.mechanicmanager.customers.repository.CustomerRepository;
import com.mechanicmanager.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UpdateCustomerUseCase {

    private final CustomerRepository customerRepository;
    private final CustomerMapper customerMapper;

    @Transactional
    public CustomerResponse execute(Long userId, Long customerId, UpdateCustomerRequest request) {
        Customer customer = customerRepository.findByIdAndUserId(customerId, userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No se encontró el cliente con id " + customerId));

        customer.setFirstName(request.firstName().trim());
        customer.setLastName(request.lastName().trim());
        customer.setDocumentNumber(normalize(request.documentNumber()));
        customer.setPhone(request.phone().trim());
        customer.setEmail(normalizeEmail(request.email()));
        customer.setAddress(normalize(request.address()));
        customer.setNotes(normalize(request.notes()));

        return customerMapper.toResponse(customerRepository.save(customer));
    }

    private static String normalizeEmail(String value) {
        String normalized = normalize(value);
        return normalized == null ? null : normalized.toLowerCase();
    }

    private static String normalize(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
