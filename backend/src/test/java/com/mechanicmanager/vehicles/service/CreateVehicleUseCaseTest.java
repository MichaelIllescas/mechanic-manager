package com.mechanicmanager.vehicles.service;

import com.mechanicmanager.customers.model.Customer;
import com.mechanicmanager.customers.repository.CustomerRepository;
import com.mechanicmanager.shared.exception.ResourceNotFoundException;
import com.mechanicmanager.vehicles.dto.CreateVehicleRequest;
import com.mechanicmanager.vehicles.exception.LicensePlateAlreadyExistsException;
import com.mechanicmanager.vehicles.mapper.VehicleMapper;
import com.mechanicmanager.vehicles.model.Vehicle;
import com.mechanicmanager.vehicles.model.VehicleType;
import com.mechanicmanager.vehicles.repository.VehicleRepository;
import com.mechanicmanager.workshops.model.Workshop;
import com.mechanicmanager.workshops.repository.WorkshopRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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
class CreateVehicleUseCaseTest {

    @Mock
    private VehicleRepository vehicleRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private WorkshopRepository workshopRepository;

    private CreateVehicleUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new CreateVehicleUseCase(
                vehicleRepository,
                customerRepository,
                workshopRepository,
                new VehicleMapper()
        );
    }

    @Test
    void createsVehicleForCustomerFromAuthenticatedUsersWorkshop() {
        Workshop workshop = Workshop.builder().id(10L).build();
        Customer customer = Customer.builder()
                .id(20L)
                .workshop(workshop)
                .firstName("Juan")
                .lastName("Pérez")
                .build();

        when(workshopRepository.findByUserId(7L)).thenReturn(Optional.of(workshop));
        when(customerRepository.findByIdAndWorkshop_Id(20L, 10L))
                .thenReturn(Optional.of(customer));
        when(vehicleRepository.existsByWorkshop_IdAndLicensePlateIgnoreCase(10L, "AB123CD"))
                .thenReturn(false);
        when(vehicleRepository.save(any(Vehicle.class))).thenAnswer(invocation -> {
            Vehicle vehicle = invocation.getArgument(0);
            vehicle.setId(30L);
            return vehicle;
        });

        var response = useCase.execute(7L, request());

        ArgumentCaptor<Vehicle> captor = ArgumentCaptor.forClass(Vehicle.class);
        verify(vehicleRepository).save(captor.capture());
        Vehicle saved = captor.getValue();

        assertThat(saved.getWorkshop()).isSameAs(workshop);
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
    void rejectsDuplicateLicensePlateInsideWorkshop() {
        Workshop workshop = Workshop.builder().id(10L).build();
        Customer customer = Customer.builder().id(20L).workshop(workshop).build();

        when(workshopRepository.findByUserId(7L)).thenReturn(Optional.of(workshop));
        when(customerRepository.findByIdAndWorkshop_Id(20L, 10L))
                .thenReturn(Optional.of(customer));
        when(vehicleRepository.existsByWorkshop_IdAndLicensePlateIgnoreCase(10L, "AB123CD"))
                .thenReturn(true);

        assertThatThrownBy(() -> useCase.execute(7L, request()))
                .isInstanceOf(LicensePlateAlreadyExistsException.class);

        verify(vehicleRepository, never()).save(any(Vehicle.class));
    }

    @Test
    void rejectsCustomerOutsideAuthenticatedUsersWorkshop() {
        Workshop workshop = Workshop.builder().id(10L).build();

        when(workshopRepository.findByUserId(7L)).thenReturn(Optional.of(workshop));
        when(customerRepository.findByIdAndWorkshop_Id(20L, 10L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(7L, request()))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("No se encontró el cliente indicado");

        verify(vehicleRepository, never()).save(any(Vehicle.class));
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
}
