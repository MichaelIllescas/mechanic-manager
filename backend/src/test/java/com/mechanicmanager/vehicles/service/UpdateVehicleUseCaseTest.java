package com.mechanicmanager.vehicles.service;

import com.mechanicmanager.customers.model.Customer;
import com.mechanicmanager.customers.repository.CustomerRepository;
import com.mechanicmanager.shared.exception.ResourceNotFoundException;
import com.mechanicmanager.vehicles.dto.UpdateVehicleRequest;
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

    @Mock
    private VehicleRepository vehicleRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private WorkshopRepository workshopRepository;

    private UpdateVehicleUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new UpdateVehicleUseCase(
                vehicleRepository,
                customerRepository,
                workshopRepository,
                new VehicleMapper()
        );
    }

    @Test
    void updatesVehicleAndAllowsCustomerFromSameWorkshop() {
        Workshop workshop = Workshop.builder().id(10L).build();
        Customer originalCustomer = customer(20L, workshop, "Ana", "Gómez");
        Customer newCustomer = customer(21L, workshop, "Luis", "López");
        Vehicle vehicle = vehicle(30L, workshop, originalCustomer);

        when(workshopRepository.findByUserId(7L)).thenReturn(Optional.of(workshop));
        when(vehicleRepository.findByIdAndWorkshop_Id(30L, 10L))
                .thenReturn(Optional.of(vehicle));
        when(customerRepository.findByIdAndWorkshop_Id(21L, 10L))
                .thenReturn(Optional.of(newCustomer));
        when(vehicleRepository.existsByWorkshop_IdAndLicensePlateIgnoreCaseAndIdNot(
                10L,
                "AC456EF",
                30L
        )).thenReturn(false);
        when(vehicleRepository.save(vehicle)).thenReturn(vehicle);

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
    void rejectsVehicleOutsideAuthenticatedUsersWorkshop() {
        Workshop workshop = Workshop.builder().id(10L).build();

        when(workshopRepository.findByUserId(7L)).thenReturn(Optional.of(workshop));
        when(vehicleRepository.findByIdAndWorkshop_Id(30L, 10L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(7L, 30L, request()))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("No se encontró el vehículo con id 30");

        verify(vehicleRepository, never()).save(any(Vehicle.class));
    }

    @Test
    void rejectsCustomerOutsideAuthenticatedUsersWorkshop() {
        Workshop workshop = Workshop.builder().id(10L).build();
        Vehicle vehicle = vehicle(30L, workshop, customer(20L, workshop, "Ana", "Gómez"));

        when(workshopRepository.findByUserId(7L)).thenReturn(Optional.of(workshop));
        when(vehicleRepository.findByIdAndWorkshop_Id(30L, 10L))
                .thenReturn(Optional.of(vehicle));
        when(customerRepository.findByIdAndWorkshop_Id(21L, 10L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(7L, 30L, request()))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("No se encontró el cliente indicado");

        verify(vehicleRepository, never()).save(any(Vehicle.class));
    }

    @Test
    void rejectsDuplicateLicensePlateInsideWorkshop() {
        Workshop workshop = Workshop.builder().id(10L).build();
        Customer customer = customer(21L, workshop, "Luis", "López");
        Vehicle vehicle = vehicle(30L, workshop, customer);

        when(workshopRepository.findByUserId(7L)).thenReturn(Optional.of(workshop));
        when(vehicleRepository.findByIdAndWorkshop_Id(30L, 10L))
                .thenReturn(Optional.of(vehicle));
        when(customerRepository.findByIdAndWorkshop_Id(21L, 10L))
                .thenReturn(Optional.of(customer));
        when(vehicleRepository.existsByWorkshop_IdAndLicensePlateIgnoreCaseAndIdNot(
                10L,
                "AC456EF",
                30L
        )).thenReturn(true);

        assertThatThrownBy(() -> useCase.execute(7L, 30L, request()))
                .isInstanceOf(LicensePlateAlreadyExistsException.class);

        verify(vehicleRepository, never()).save(any(Vehicle.class));
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
                .build();
    }

    private static Vehicle vehicle(Long id, Workshop workshop, Customer customer) {
        return Vehicle.builder()
                .id(id)
                .workshop(workshop)
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
