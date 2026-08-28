package com.mechanicmanager.vehicles.service;

import com.mechanicmanager.customers.model.Customer;
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
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VehicleQueryUseCasesTest {

    @Mock
    private VehicleRepository vehicleRepository;

    @Mock
    private WorkshopRepository workshopRepository;

    private ListVehiclesUseCase listUseCase;
    private SearchVehiclesUseCase searchUseCase;

    @BeforeEach
    void setUp() {
        VehicleMapper mapper = new VehicleMapper();
        listUseCase = new ListVehiclesUseCase(
                vehicleRepository,
                workshopRepository,
                mapper
        );
        searchUseCase = new SearchVehiclesUseCase(
                vehicleRepository,
                workshopRepository,
                mapper
        );
    }

    @Test
    void listsOnlyVehiclesFromAuthenticatedUsersWorkshop() {
        Workshop workshop = Workshop.builder().id(12L).build();
        Vehicle vehicle = vehicle(1L, workshop, "AB123CD");

        when(workshopRepository.findByUserId(3L)).thenReturn(Optional.of(workshop));
        when(vehicleRepository.findAllByWorkshop_IdOrderByLicensePlateAsc(12L))
                .thenReturn(List.of(vehicle));

        var result = listUseCase.execute(3L);

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().licensePlate()).isEqualTo("AB123CD");
        verify(vehicleRepository).findAllByWorkshop_IdOrderByLicensePlateAsc(12L);
    }

    @Test
    void searchesLicensePlateOnlyInsideAuthenticatedUsersWorkshop() {
        Workshop workshop = Workshop.builder().id(15L).build();
        Vehicle vehicle = vehicle(2L, workshop, "AC456EF");

        when(workshopRepository.findByUserId(4L)).thenReturn(Optional.of(workshop));
        when(vehicleRepository.findAll(
                org.mockito.ArgumentMatchers.<Specification<Vehicle>>any(),
                eq(Sort.by("licensePlate"))
        )).thenReturn(List.of(vehicle));

        var result = searchUseCase.execute(4L, "  AC456  ");

        assertThat(result)
                .extracting(response -> response.licensePlate())
                .containsExactly("AC456EF");
        verify(vehicleRepository).findAll(
                org.mockito.ArgumentMatchers.<Specification<Vehicle>>any(),
                eq(Sort.by("licensePlate"))
        );
    }

    private static Vehicle vehicle(Long id, Workshop workshop, String licensePlate) {
        Customer customer = Customer.builder()
                .id(20L)
                .workshop(workshop)
                .firstName("Ana")
                .lastName("Gómez")
                .build();

        return Vehicle.builder()
                .id(id)
                .workshop(workshop)
                .customer(customer)
                .licensePlate(licensePlate)
                .brand("Toyota")
                .model("Corolla")
                .chassisNumber("CHASIS123")
                .engineNumber("MOTOR456")
                .vehicleType(VehicleType.CAR)
                .build();
    }
}
