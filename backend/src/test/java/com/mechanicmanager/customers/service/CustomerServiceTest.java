package com.mechanicmanager.customers.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {

    @Mock
    private CreateCustomerUseCase createCustomerUseCase;

    @Mock
    private ListCustomersUseCase listCustomersUseCase;

    @Mock
    private SearchCustomersUseCase searchCustomersUseCase;

    @Mock
    private UpdateCustomerUseCase updateCustomerUseCase;

    private CustomerService customerService;

    @BeforeEach
    void setUp() {
        customerService = new CustomerService(
                createCustomerUseCase,
                listCustomersUseCase,
                searchCustomersUseCase,
                updateCustomerUseCase
        );
    }

    @Test
    void blankSearchUsesCustomerList() {
        when(listCustomersUseCase.execute(3L)).thenReturn(List.of());

        customerService.findAll(3L, "  ");

        verify(listCustomersUseCase).execute(3L);
        verifyNoInteractions(searchCustomersUseCase);
    }

    @Test
    void nonBlankSearchUsesCustomerSearch() {
        when(searchCustomersUseCase.execute(3L, "Ana")).thenReturn(List.of());

        customerService.findAll(3L, "Ana");

        verify(searchCustomersUseCase).execute(3L, "Ana");
        verifyNoInteractions(listCustomersUseCase);
    }
}
