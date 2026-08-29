package com.mechanicmanager.customers.service;

import com.mechanicmanager.customers.dto.CustomerResponse;
import com.mechanicmanager.customers.mapper.CustomerMapper;
import com.mechanicmanager.customers.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ListCustomersUseCase {

    private final CustomerRepository customerRepository;
    private final CustomerMapper customerMapper;

    @Transactional(readOnly = true)
    public List<CustomerResponse> execute(Long userId) {
        return customerRepository
                .findAllByUserIdOrderByLastNameAscFirstNameAsc(userId)
                .stream()
                .map(customerMapper::toResponse)
                .toList();
    }
}
