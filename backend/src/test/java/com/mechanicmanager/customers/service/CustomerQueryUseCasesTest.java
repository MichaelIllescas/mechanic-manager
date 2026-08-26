package com.mechanicmanager.customers.service;

import com.mechanicmanager.customers.mapper.CustomerMapper;
import com.mechanicmanager.customers.model.Customer;
import com.mechanicmanager.customers.repository.CustomerRepository;
import com.mechanicmanager.workshops.model.Workshop;
import com.mechanicmanager.workshops.repository.WorkshopRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomerQueryUseCasesTest {

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private WorkshopRepository workshopRepository;

    private ListCustomersUseCase listUseCase;
    private SearchCustomersUseCase searchUseCase;

    @BeforeEach
    void setUp() {
        CustomerMapper mapper = new CustomerMapper();
        listUseCase = new ListCustomersUseCase(
                customerRepository,
                workshopRepository,
                mapper
        );
        searchUseCase = new SearchCustomersUseCase(
                customerRepository,
                workshopRepository,
                mapper
        );
    }

    @Test
    void listsOnlyCustomersFromAuthenticatedUsersWorkshop() {
        Workshop workshop = Workshop.builder().id(12L).build();
        Customer customer = customer(1L, workshop, "Ana", "Gómez");

        when(workshopRepository.findByUserId(3L)).thenReturn(Optional.of(workshop));
        when(customerRepository.findAllByWorkshop_IdOrderByLastNameAscFirstNameAsc(12L))
                .thenReturn(List.of(customer));

        var result = listUseCase.execute(3L);

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().workshopId()).isEqualTo(12L);
        verify(customerRepository)
                .findAllByWorkshop_IdOrderByLastNameAscFirstNameAsc(12L);
    }

    @Test
    void searchesOnlyInsideAuthenticatedUsersWorkshop() {
        Workshop workshop = Workshop.builder().id(15L).build();
        Customer customer = customer(2L, workshop, "Luis", "López");

        when(workshopRepository.findByUserId(4L)).thenReturn(Optional.of(workshop));
        when(customerRepository.findAll(
                org.mockito.ArgumentMatchers.<Specification<Customer>>any(),
                eq(Sort.by("lastName", "firstName"))
        ))
                .thenReturn(List.of(customer));

        var result = searchUseCase.execute(4L, "  Luis  ");

        assertThat(result).extracting(response -> response.firstName())
                .containsExactly("Luis");
        verify(customerRepository).findAll(
                org.mockito.ArgumentMatchers.<Specification<Customer>>any(),
                eq(Sort.by("lastName", "firstName"))
        );
    }

    private static Customer customer(
            Long id,
            Workshop workshop,
            String firstName,
            String lastName
    ) {
        return Customer.builder()
                .id(id)
                .workshop(workshop)
                .firstName(firstName)
                .lastName(lastName)
                .phone("11 4444-5555")
                .build();
    }
}
