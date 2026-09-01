package com.mechanicmanager.vehicles.service;

import com.mechanicmanager.customers.model.Customer;
import com.mechanicmanager.customers.repository.CustomerRepository;
import com.mechanicmanager.shared.exception.ResourceNotFoundException;
import com.mechanicmanager.users.model.User;
import com.mechanicmanager.users.repository.UserRepository;
import com.mechanicmanager.vehicles.dto.CreateVehicleRequest;
import com.mechanicmanager.vehicles.exception.LicensePlateAlreadyExistsException;
import com.mechanicmanager.vehicles.mapper.VehicleMapper;
import com.mechanicmanager.vehicles.model.Vehicle;
import com.mechanicmanager.vehicles.model.VehicleType;
import com.mechanicmanager.vehicles.repository.VehicleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateVehicleUseCaseTest {

    @Mock
    private VehicleRepository vehicleRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private UserRepository userRepository;

    private CreateVehicleUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new CreateVehicleUseCase(
                vehicleRepository,
                customerRepository,
                userRepository,
                new VehicleMapper()
        );
    }

    @Test
    void createsVehicleForCustomerFromAuthenticatedUser() {
        User user = User.builder().id(7L).build();
        Customer customer = Customer.builder()
                .id(20L)
                .user(user)
                .firstName("Juan")
                .lastName("Pérez")
                .build();

        when(customerRepository.findByIdAndUserId(20L, 7L))
                .thenReturn(Optional.of(customer));
        when(vehicleRepository.existsByUserIdAndLicensePlateIgnoreCase(7L, "AB123CD"))
                .thenReturn(false);
        when(userRepository.getReferenceById(7L)).thenReturn(user);
        when(vehicleRepository.saveAndFlush(any(Vehicle.class))).thenAnswer(invocation -> {
            Vehicle vehicle = invocation.getArgument(0);
            vehicle.setId(30L);
            return vehicle;
        });

        var response = useCase.execute(7L, request());

        ArgumentCaptor<Vehicle> captor = ArgumentCaptor.forClass(Vehicle.class);
        verify(vehicleRepository).saveAndFlush(captor.capture());
        Vehicle saved = captor.getValue();

        assertThat(saved.getUser()).isSameAs(user);
        assertThat(saved.getCustomer()).isSameAs(customer);
        assertThat(saved.getLicensePlate()).isEqualTo("AB123CD");
        assertThat(saved.getChassisNumber()).isEqualTo("CHASIS123");
        assertThat(saved.getEngineNumber()).isEqualTo("MOTOR456");
        assertThat(saved.getYear()).isNull();
        assertThat(saved.getMileage()).isNull();
        assertThat(saved.getNotes()).isNull();
        assertThat(response.id()).isEqualTo(30L);
        assertThat(response.customerId()).isEqualTo(20L);
    }

    @Test
    void createsVehicleWithoutTechnicalIdentifiers() {
        User user = User.builder().id(7L).build();
        Customer customer = Customer.builder()
                .id(20L)
                .user(user)
                .firstName("Juan")
                .lastName("Pérez")
                .build();

        when(customerRepository.findByIdAndUserId(20L, 7L))
                .thenReturn(Optional.of(customer));
        when(vehicleRepository.existsByUserIdAndLicensePlateIgnoreCase(7L, "AB123CD"))
                .thenReturn(false);
        when(userRepository.getReferenceById(7L)).thenReturn(user);
        when(vehicleRepository.saveAndFlush(any(Vehicle.class))).thenAnswer(invocation -> {
            Vehicle vehicle = invocation.getArgument(0);
            vehicle.setId(30L);
            return vehicle;
        });

        useCase.execute(7L, requestWithoutTechnicalIdentifiers());

        ArgumentCaptor<Vehicle> captor = ArgumentCaptor.forClass(Vehicle.class);
        verify(vehicleRepository).saveAndFlush(captor.capture());
        Vehicle saved = captor.getValue();

        assertThat(saved.getChassisNumber()).isNull();
        assertThat(saved.getEngineNumber()).isNull();
    }

    @Test
    void rejectsDuplicateLicensePlateForSameUser() {
        Customer customer = Customer.builder().id(20L).build();

        when(customerRepository.findByIdAndUserId(20L, 7L))
                .thenReturn(Optional.of(customer));
        when(vehicleRepository.existsByUserIdAndLicensePlateIgnoreCase(7L, "AB123CD"))
                .thenReturn(true);

        assertThatThrownBy(() -> useCase.execute(7L, request()))
                .isInstanceOf(LicensePlateAlreadyExistsException.class);

        verify(vehicleRepository, never()).saveAndFlush(any(Vehicle.class));
    }

    @Test
    void rejectsCustomerFromAnotherUser() {
        when(customerRepository.findByIdAndUserId(20L, 7L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(7L, request()))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("No se encontró el cliente indicado");

        verify(vehicleRepository, never()).saveAndFlush(any(Vehicle.class));
    }

    @Test
    void translatesConcurrentDuplicateLicensePlateIntoDomainConflict() {
        User user = User.builder().id(7L).build();
        Customer customer = Customer.builder().id(20L).build();

        when(customerRepository.findByIdAndUserId(20L, 7L))
                .thenReturn(Optional.of(customer));
        when(vehicleRepository.existsByUserIdAndLicensePlateIgnoreCase(7L, "AB123CD"))
                .thenReturn(false);
        when(userRepository.getReferenceById(7L)).thenReturn(user);
        when(vehicleRepository.saveAndFlush(any(Vehicle.class))).thenThrow(
                new DataIntegrityViolationException(
                        "Duplicate entry for key 'uq_vehicles_user_license_plate'")
        );

        assertThatThrownBy(() -> useCase.execute(7L, request()))
                .isInstanceOf(LicensePlateAlreadyExistsException.class);
    }

    @Test
    void doesNotDisguiseUnrelatedIntegrityViolations() {
        User user = User.builder().id(7L).build();
        Customer customer = Customer.builder().id(20L).build();

        when(customerRepository.findByIdAndUserId(20L, 7L))
                .thenReturn(Optional.of(customer));
        when(vehicleRepository.existsByUserIdAndLicensePlateIgnoreCase(7L, "AB123CD"))
                .thenReturn(false);
        when(userRepository.getReferenceById(7L)).thenReturn(user);
        when(vehicleRepository.saveAndFlush(any(Vehicle.class))).thenThrow(
                new DataIntegrityViolationException(
                        "Cannot add or update a child row: fk_vehicles_customer_user")
        );

        assertThatThrownBy(() -> useCase.execute(7L, request()))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    private static CreateVehicleRequest request() {
        return new CreateVehicleRequest(
                20L,
                " ab123cd ",
                " Toyota ",
                " Corolla ",
                null,
                " chasis123 ",
                " motor456 ",
                null,
                VehicleType.CAR,
                "  "
        );
    }

    private static CreateVehicleRequest requestWithoutTechnicalIdentifiers() {
        return new CreateVehicleRequest(
                20L,
                " ab123cd ",
                " Toyota ",
                " Corolla ",
                null,
                null,
                "  ",
                null,
                VehicleType.CAR,
                null
        );
    }
}
