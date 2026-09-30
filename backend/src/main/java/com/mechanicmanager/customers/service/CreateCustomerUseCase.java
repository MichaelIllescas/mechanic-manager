package com.mechanicmanager.customers.service;

import com.mechanicmanager.customers.dto.CreateCustomerRequest;
import com.mechanicmanager.customers.dto.CustomerResponse;
import com.mechanicmanager.customers.mapper.CustomerMapper;
import com.mechanicmanager.customers.model.Customer;
import com.mechanicmanager.customers.repository.CustomerRepository;
import com.mechanicmanager.shared.text.Texts;
import com.mechanicmanager.users.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CreateCustomerUseCase {

    private final CustomerRepository customerRepository;
    private final UserRepository userRepository;
    private final CustomerMapper customerMapper;

    @Transactional
    public CustomerResponse execute(Long userId, CreateCustomerRequest request) {
        Customer customer = Customer.builder()
                .user(userRepository.getReferenceById(userId))
                .firstName(request.firstName().trim())
                .lastName(request.lastName().trim())
                .documentNumber(Texts.trimToNull(request.documentNumber()))
                .phone(request.phone().trim())
                .email(normalizeEmail(request.email()))
                .address(Texts.trimToNull(request.address()))
                .notes(Texts.trimToNull(request.notes()))
                .build();

        return customerMapper.toResponse(customerRepository.save(customer));
    }

    private static String normalizeEmail(String value) {
        String normalized = Texts.trimToNull(value);
        return normalized == null ? null : normalized.toLowerCase();
    }
}
