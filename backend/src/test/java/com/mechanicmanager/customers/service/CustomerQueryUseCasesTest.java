package com.mechanicmanager.customers.service;

import com.mechanicmanager.customers.mapper.CustomerMapper;
import com.mechanicmanager.customers.model.Customer;
import com.mechanicmanager.customers.repository.CustomerRepository;
import com.mechanicmanager.users.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomerQueryUseCasesTest {

    @Mock
    private CustomerRepository customerRepository;

    private ListCustomersUseCase listUseCase;
    private SearchCustomersUseCase searchUseCase;

    @BeforeEach
    void setUp() {
        CustomerMapper mapper = new CustomerMapper();
        listUseCase = new ListCustomersUseCase(customerRepository, mapper);
        searchUseCase = new SearchCustomersUseCase(customerRepository, mapper);
    }

    @Test
    void listsOnlyCustomersFromAuthenticatedUser() {
        Customer customer = customer(1L, User.builder().id(3L).build(), "Ana", "Gómez");

        when(customerRepository.findAllByUserIdOrderByLastNameAscFirstNameAsc(3L))
                .thenReturn(List.of(customer));

        var result = listUseCase.execute(3L);

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().firstName()).isEqualTo("Ana");
        verify(customerRepository)
                .findAllByUserIdOrderByLastNameAscFirstNameAsc(3L);
    }

    @Test
    void searchesOnlyInsideAuthenticatedUsersCustomers() {
        Customer customer = customer(2L, User.builder().id(4L).build(), "Luis", "López");

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
            User user,
            String firstName,
            String lastName
    ) {
        return Customer.builder()
                .id(id)
                .user(user)
                .firstName(firstName)
                .lastName(lastName)
                .phone("11 4444-5555")
                .build();
    }
}
