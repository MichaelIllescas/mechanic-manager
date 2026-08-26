package com.mechanicmanager.customers.service;

import com.mechanicmanager.customers.dto.CustomerResponse;
import com.mechanicmanager.customers.mapper.CustomerMapper;
import com.mechanicmanager.customers.repository.CustomerRepository;
import com.mechanicmanager.customers.repository.CustomerSpecifications;
import com.mechanicmanager.shared.exception.ResourceNotFoundException;
import com.mechanicmanager.workshops.model.Workshop;
import com.mechanicmanager.workshops.repository.WorkshopRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SearchCustomersUseCase {

    private final CustomerRepository customerRepository;
    private final WorkshopRepository workshopRepository;
    private final CustomerMapper customerMapper;

    @Transactional(readOnly = true)
    public List<CustomerResponse> execute(Long userId, String search) {
        Workshop workshop = workshopRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "El usuario no tiene un taller asociado"));

        String normalizedSearch = search.trim();
        return customerRepository.findAll(
                        CustomerSpecifications.belongsToWorkshopAndMatches(
                                workshop.getId(),
                                normalizedSearch
                        ),
                        Sort.by("lastName", "firstName")
                )
                .stream()
                .map(customerMapper::toResponse)
                .toList();
    }
}
