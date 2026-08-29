package com.mechanicmanager.vehicles.service;

import com.mechanicmanager.customers.model.Customer;
import com.mechanicmanager.users.model.User;
import com.mechanicmanager.vehicles.mapper.VehicleMapper;
import com.mechanicmanager.vehicles.model.Vehicle;
import com.mechanicmanager.vehicles.model.VehicleType;
import com.mechanicmanager.vehicles.repository.VehicleRepository;
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
class VehicleQueryUseCasesTest {

    @Mock
    private VehicleRepository vehicleRepository;

    private ListVehiclesUseCase listUseCase;
    private SearchVehiclesUseCase searchUseCase;

    @BeforeEach
    void setUp() {
        VehicleMapper mapper = new VehicleMapper();
        listUseCase = new ListVehiclesUseCase(vehicleRepository, mapper);
        searchUseCase = new SearchVehiclesUseCase(vehicleRepository, mapper);
    }

    @Test
    void listsOnlyVehiclesFromAuthenticatedUser() {
        Vehicle vehicle = vehicle(1L, User.builder().id(3L).build(), "AB123CD");

        when(vehicleRepository.findAllByUserIdOrderByLicensePlateAsc(3L))
                .thenReturn(List.of(vehicle));

        var result = listUseCase.execute(3L);

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().licensePlate()).isEqualTo("AB123CD");
        verify(vehicleRepository).findAllByUserIdOrderByLicensePlateAsc(3L);
    }

    @Test
    void searchesLicensePlateOnlyInsideAuthenticatedUsersVehicles() {
        Vehicle vehicle = vehicle(2L, User.builder().id(4L).build(), "AC456EF");

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

    private static Vehicle vehicle(Long id, User user, String licensePlate) {
        Customer customer = Customer.builder()
                .id(20L)
                .user(user)
                .firstName("Ana")
                .lastName("Gómez")
                .build();

        return Vehicle.builder()
                .id(id)
                .user(user)
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
