package com.mechanicmanager.vehicles.service;

import com.mechanicmanager.vehicles.dto.CreateVehicleRequest;
import com.mechanicmanager.vehicles.dto.UpdateVehicleRequest;
import com.mechanicmanager.vehicles.model.VehicleType;
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
class VehicleServiceTest {

    @Mock
    private CreateVehicleUseCase createVehicleUseCase;

    @Mock
    private ListVehiclesUseCase listVehiclesUseCase;

    @Mock
    private SearchVehiclesUseCase searchVehiclesUseCase;

    @Mock
    private UpdateVehicleUseCase updateVehicleUseCase;

    private VehicleService vehicleService;

    @BeforeEach
    void setUp() {
        vehicleService = new VehicleService(
                createVehicleUseCase,
                listVehiclesUseCase,
                searchVehiclesUseCase,
                updateVehicleUseCase
        );
    }

    @Test
    void delegatesVehicleCreation() {
        CreateVehicleRequest request = createRequest();

        vehicleService.create(3L, request);

        verify(createVehicleUseCase).execute(3L, request);
    }

    @Test
    void blankSearchUsesVehicleList() {
        when(listVehiclesUseCase.execute(3L)).thenReturn(List.of());

        vehicleService.findAll(3L, "  ");

        verify(listVehiclesUseCase).execute(3L);
        verifyNoInteractions(searchVehiclesUseCase);
    }

    @Test
    void nonBlankSearchUsesVehicleSearch() {
        when(searchVehiclesUseCase.execute(3L, "AB123"))
                .thenReturn(List.of());

        vehicleService.findAll(3L, "AB123");

        verify(searchVehiclesUseCase).execute(3L, "AB123");
        verifyNoInteractions(listVehiclesUseCase);
    }

    @Test
    void delegatesVehicleUpdate() {
        UpdateVehicleRequest request = updateRequest();

        vehicleService.update(3L, 8L, request);

        verify(updateVehicleUseCase).execute(3L, 8L, request);
    }

    private static CreateVehicleRequest createRequest() {
        return new CreateVehicleRequest(
                2L,
                "AB123CD",
                "Toyota",
                "Corolla",
                null,
                "CHASIS123",
                "MOTOR456",
                null,
                VehicleType.CAR,
                null
        );
    }

    private static UpdateVehicleRequest updateRequest() {
        return new UpdateVehicleRequest(
                2L,
                "AB123CD",
                "Toyota",
                "Corolla",
                null,
                "CHASIS123",
                "MOTOR456",
                null,
                VehicleType.CAR,
                null
        );
    }
}
