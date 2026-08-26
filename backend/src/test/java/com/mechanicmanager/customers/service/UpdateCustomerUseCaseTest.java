package com.mechanicmanager.customers.service;

import com.mechanicmanager.customers.dto.UpdateCustomerRequest;
import com.mechanicmanager.customers.mapper.CustomerMapper;
import com.mechanicmanager.customers.model.Customer;
import com.mechanicmanager.customers.repository.CustomerRepository;
import com.mechanicmanager.shared.exception.ResourceNotFoundException;
import com.mechanicmanager.workshops.model.Workshop;
import com.mechanicmanager.workshops.repository.WorkshopRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UpdateCustomerUseCaseTest {

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private WorkshopRepository workshopRepository;

    private UpdateCustomerUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new UpdateCustomerUseCase(
                customerRepository,
                workshopRepository,
                new CustomerMapper()
        );
    }

    @Test
    void updatesCustomerBelongingToAuthenticatedUsersWorkshop() {
        Workshop workshop = Workshop.builder().id(20L).build();
        Customer customer = Customer.builder()
                .id(8L)
                .workshop(workshop)
                .firstName("Nombre anterior")
                .lastName("Apellido anterior")
                .phone("11 1111-1111")
                .build();
        UpdateCustomerRequest request = new UpdateCustomerRequest(
                "  María ",
                " Suárez  ",
                " ",
                "11 5555-6666",
                " MARIA@EMAIL.COM ",
                " Cliente habitual "
        );

        when(workshopRepository.findByUserId(5L)).thenReturn(Optional.of(workshop));
        when(customerRepository.findByIdAndWorkshop_Id(8L, 20L))
                .thenReturn(Optional.of(customer));
        when(customerRepository.save(customer)).thenReturn(customer);

        var response = useCase.execute(5L, 8L, request);

        assertThat(response.firstName()).isEqualTo("María");
        assertThat(response.lastName()).isEqualTo("Suárez");
        assertThat(response.documentNumber()).isNull();
        assertThat(response.email()).isEqualTo("maria@email.com");
        assertThat(response.notes()).isEqualTo("Cliente habitual");
        verify(customerRepository).findByIdAndWorkshop_Id(8L, 20L);
        verify(customerRepository).save(customer);
    }

    @Test
    void doesNotRevealOrUpdateCustomerFromAnotherWorkshop() {
        Workshop workshop = Workshop.builder().id(20L).build();
        UpdateCustomerRequest request = new UpdateCustomerRequest(
                "María",
                "Suárez",
                null,
                "11 5555-6666",
                null,
                null
        );

        when(workshopRepository.findByUserId(5L)).thenReturn(Optional.of(workshop));
        when(customerRepository.findByIdAndWorkshop_Id(99L, 20L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(5L, 99L, request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("No se encontró el cliente con id 99");

        verify(customerRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }
}
