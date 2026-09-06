package com.mechanicmanager.appointments.service;

import com.mechanicmanager.appointments.mapper.AppointmentMapper;
import com.mechanicmanager.appointments.model.AppointmentStatus;
import com.mechanicmanager.appointments.repository.AppointmentRepository;
import com.mechanicmanager.shared.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static com.mechanicmanager.appointments.service.AppointmentFixtures.appointmentWith;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FindAppointmentUseCaseTest {

    @Mock
    private AppointmentRepository appointmentRepository;

    private FindAppointmentUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new FindAppointmentUseCase(
                appointmentRepository,
                new AppointmentMapper()
        );
    }

    @Test
    void returnsAppointmentWithVehicleAndCustomerData() {
        when(appointmentRepository.findByIdAndUserId(50L, 7L))
                .thenReturn(Optional.of(appointmentWith(AppointmentStatus.SCHEDULED)));

        var response = useCase.execute(7L, 50L);

        assertThat(response.id()).isEqualTo(50L);
        assertThat(response.status()).isEqualTo(AppointmentStatus.SCHEDULED);
        assertThat(response.vehicleId()).isEqualTo(30L);
        assertThat(response.licensePlate()).isEqualTo("AB123CD");
        assertThat(response.customerLastName()).isEqualTo("Pérez");
    }

    @Test
    void rejectsAppointmentFromAnotherUser() {
        when(appointmentRepository.findByIdAndUserId(50L, 7L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(7L, 50L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("No se encontró el turno indicado");
    }
}
