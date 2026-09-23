package com.mechanicmanager.appointments.service;

import com.mechanicmanager.appointments.dto.AppointmentResponse;
import com.mechanicmanager.appointments.dto.CreateAppointmentRequest;
import com.mechanicmanager.appointments.dto.UpdateAppointmentRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AppointmentService {

    private final CreateAppointmentUseCase createAppointmentUseCase;
    private final FindAppointmentUseCase findAppointmentUseCase;
    private final ListAppointmentsByRangeUseCase listAppointmentsByRangeUseCase;
    private final UpdateAppointmentUseCase updateAppointmentUseCase;
    private final CompleteAppointmentUseCase completeAppointmentUseCase;
    private final CancelAppointmentUseCase cancelAppointmentUseCase;
    private final DeleteAppointmentUseCase deleteAppointmentUseCase;

    public AppointmentResponse create(Long userId, CreateAppointmentRequest request) {
        return createAppointmentUseCase.execute(userId, request);
    }

    public AppointmentResponse findById(Long userId, Long appointmentId) {
        return findAppointmentUseCase.execute(userId, appointmentId);
    }

    public List<AppointmentResponse> findByRange(Long userId, LocalDate from, LocalDate to) {
        return listAppointmentsByRangeUseCase.execute(userId, from, to);
    }

    public AppointmentResponse update(
            Long userId,
            Long appointmentId,
            UpdateAppointmentRequest request
    ) {
        return updateAppointmentUseCase.execute(userId, appointmentId, request);
    }

    public AppointmentResponse complete(Long userId, Long appointmentId) {
        return completeAppointmentUseCase.execute(userId, appointmentId);
    }

    public AppointmentResponse cancel(Long userId, Long appointmentId) {
        return cancelAppointmentUseCase.execute(userId, appointmentId);
    }

    public void delete(Long userId, Long appointmentId) {
        deleteAppointmentUseCase.execute(userId, appointmentId);
    }
}
