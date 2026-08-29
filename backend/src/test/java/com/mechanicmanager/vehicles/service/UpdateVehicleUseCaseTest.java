package com.mechanicmanager.vehicles.service;

import com.mechanicmanager.customers.model.Customer;
import com.mechanicmanager.customers.repository.CustomerRepository;
import com.mechanicmanager.shared.exception.ResourceNotFoundException;
import com.mechanicmanager.users.model.User;
import com.mechanicmanager.vehicles.dto.UpdateVehicleRequest;
import com.mechanicmanager.vehicles.exception.LicensePlateAlreadyExistsException;
import com.mechanicmanager.vehicles.mapper.VehicleMapper;
import com.mechanicmanager.vehicles.model.Vehicle;
import com.mechanicmanager.vehicles.model.VehicleType;
import com.mechanicmanager.vehicles.repository.VehicleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UpdateVehicleUseCaseTest {

    private static final User USER = User.builder().id(7L).build();

    @Mock
    private VehicleRepository vehicleRepository;

    @Mock
    private CustomerRepository customerRepository;

    private UpdateVehicleUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new UpdateVehicleUseCase(
                vehicleRepository,
                customerRepository,
                new VehicleMapper()
        );
    }

    @Test
    void updatesVehicleAndAllowsCustomerFromSameUser() {
        Customer originalCustomer = customer(20L, "Ana", "Gómez");
        Customer newCustomer = customer(21L, "Luis", "López");
        Vehicle vehicle = vehicle(30L, originalCustomer);

        when(vehicleRepository.findByIdAndUserId(30L, 7L))
                .thenReturn(Optional.of(vehicle));
        when(customerRepository.findByIdAndUserId(21L, 7L))
                .thenReturn(Optional.of(newCustomer));
        when(vehicleRepository.existsByUserIdAndLicensePlateIgnoreCaseAndIdNot(
                7L,
                "AC456EF",
                30L
        )).thenReturn(false);
        when(vehicleRepository.saveAndFlush(vehicle)).thenReturn(vehicle);

        var response = useCase.execute(7L, 30L, request());

        assertThat(vehicle.getCustomer()).isSameAs(newCustomer);
        assertThat(vehicle.getLicensePlate()).isEqualTo("AC456EF");
        assertThat(vehicle.getBrand()).isEqualTo("Ford");
        assertThat(vehicle.getModel()).isEqualTo("Ranger");
        assertThat(vehicle.getYear()).isNull();
        assertThat(vehicle.getMileage()).isNull();
        assertThat(vehicle.getVehicleType()).isEqualTo(VehicleType.PICKUP);
        assertThat(vehicle.getNotes()).isNull();
        assertThat(response.customerId()).isEqualTo(21L);
    }

    @Test
    void rejectsVehicleFromAnotherUser() {
        when(vehicleRepository.findByIdAndUserId(30L, 7L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(7L, 30L, request()))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("No se encontró el vehículo con id 30");

        verify(vehicleRepository, never()).saveAndFlush(any(Vehicle.class));
    }

    @Test
    void rejectsCustomerFromAnotherUser() {
        Vehicle vehicle = vehicle(30L, customer(20L, "Ana", "Gómez"));

        when(vehicleRepository.findByIdAndUserId(30L, 7L))
                .thenReturn(Optional.of(vehicle));
        when(customerRepository.findByIdAndUserId(21L, 7L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(7L, 30L, request()))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("No se encontró el cliente indicado");

        verify(vehicleRepository, never()).saveAndFlush(any(Vehicle.class));
    }

    @Test
    void rejectsDuplicateLicensePlateForSameUser() {
        Customer customer = customer(21L, "Luis", "López");
        Vehicle vehicle = vehicle(30L, customer);

        when(vehicleRepository.findByIdAndUserId(30L, 7L))
                .thenReturn(Optional.of(vehicle));
        when(customerRepository.findByIdAndUserId(21L, 7L))
                .thenReturn(Optional.of(customer));
        when(vehicleRepository.existsByUserIdAndLicensePlateIgnoreCaseAndIdNot(
                7L,
                "AC456EF",
                30L
        )).thenReturn(true);

        assertThatThrownBy(() -> useCase.execute(7L, 30L, request()))
                .isInstanceOf(LicensePlateAlreadyExistsException.class);

        verify(vehicleRepository, never()).saveAndFlush(any(Vehicle.class));
    }

    private static UpdateVehicleRequest request() {
        return new UpdateVehicleRequest(
                21L,
                " ac456ef ",
                " Ford ",
                " Ranger ",
                null,
                " chasis789 ",
                " motor012 ",
                null,
                VehicleType.PICKUP,
                "  "
        );
    }

    private static Customer customer(Long id, String firstName, String lastName) {
        return Customer.builder()
                .id(id)
                .user(USER)
                .firstName(firstName)
                .lastName(lastName)
                .build();
    }

    private static Vehicle vehicle(Long id, Customer customer) {
        return Vehicle.builder()
                .id(id)
                .user(USER)
                .customer(customer)
                .licensePlate("AB123CD")
                .brand("Toyota")
                .model("Corolla")
                .chassisNumber("CHASIS123")
                .engineNumber("MOTOR456")
                .vehicleType(VehicleType.CAR)
                .build();
    }
}
