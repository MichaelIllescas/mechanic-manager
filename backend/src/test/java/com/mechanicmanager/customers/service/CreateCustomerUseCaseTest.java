package com.mechanicmanager.customers.service;

import com.mechanicmanager.customers.dto.CreateCustomerRequest;
import com.mechanicmanager.customers.mapper.CustomerMapper;
import com.mechanicmanager.customers.model.Customer;
import com.mechanicmanager.customers.repository.CustomerRepository;
import com.mechanicmanager.users.model.User;
import com.mechanicmanager.users.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateCustomerUseCaseTest {

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private UserRepository userRepository;

    private CreateCustomerUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new CreateCustomerUseCase(
                customerRepository,
                userRepository,
                new CustomerMapper()
        );
    }

    @Test
    void createsCustomerForAuthenticatedUserAndNormalizesData() {
        User user = User.builder().id(7L).build();
        CreateCustomerRequest request = new CreateCustomerRequest(
                "  Juan  ",
                "  Pérez  ",
                "  30111222  ",
                " 11 4444-5555 ",
                "  JUAN@EMAIL.COM  ",
                " Av. Siempre Viva 742 ",
                "   "
        );

        when(userRepository.getReferenceById(7L)).thenReturn(user);
        when(customerRepository.save(any(Customer.class))).thenAnswer(invocation -> {
            Customer customer = invocation.getArgument(0);
            customer.setId(25L);
            return customer;
        });

        var response = useCase.execute(7L, request);

        ArgumentCaptor<Customer> captor = ArgumentCaptor.forClass(Customer.class);
        verify(customerRepository).save(captor.capture());
        Customer saved = captor.getValue();

        assertThat(saved.getUser()).isSameAs(user);
        assertThat(saved.getFirstName()).isEqualTo("Juan");
        assertThat(saved.getLastName()).isEqualTo("Pérez");
        assertThat(saved.getDocumentNumber()).isEqualTo("30111222");
        assertThat(saved.getPhone()).isEqualTo("11 4444-5555");
        assertThat(saved.getEmail()).isEqualTo("juan@email.com");
        assertThat(saved.getAddress()).isEqualTo("Av. Siempre Viva 742");
        assertThat(saved.getNotes()).isNull();
        assertThat(response.id()).isEqualTo(25L);
        assertThat(response.address()).isEqualTo("Av. Siempre Viva 742");
    }
}
