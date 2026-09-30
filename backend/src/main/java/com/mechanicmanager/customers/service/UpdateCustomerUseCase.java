package com.mechanicmanager.customers.service;

import com.mechanicmanager.customers.dto.CustomerResponse;
import com.mechanicmanager.customers.dto.UpdateCustomerRequest;
import com.mechanicmanager.customers.mapper.CustomerMapper;
import com.mechanicmanager.customers.model.Customer;
import com.mechanicmanager.customers.repository.CustomerRepository;
import com.mechanicmanager.shared.exception.ResourceNotFoundException;
import com.mechanicmanager.shared.text.Texts;
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
        customer.setDocumentNumber(Texts.trimToNull(request.documentNumber()));
        customer.setPhone(request.phone().trim());
        customer.setEmail(normalizeEmail(request.email()));
        customer.setAddress(Texts.trimToNull(request.address()));
        customer.setNotes(Texts.trimToNull(request.notes()));

        return customerMapper.toResponse(customerRepository.save(customer));
    }

    private static String normalizeEmail(String value) {
        String normalized = Texts.trimToNull(value);
        return normalized == null ? null : normalized.toLowerCase();
    }
}
