package com.mechanicmanager.appointments.service;

import com.mechanicmanager.appointments.exception.InvalidDateRangeException;
import com.mechanicmanager.appointments.mapper.AppointmentMapper;
import com.mechanicmanager.appointments.model.AppointmentStatus;
import com.mechanicmanager.appointments.repository.AppointmentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static com.mechanicmanager.appointments.service.AppointmentFixtures.appointmentWith;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ListAppointmentsByRangeUseCaseTest {

    @Mock
    private AppointmentRepository appointmentRepository;

    private ListAppointmentsByRangeUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new ListAppointmentsByRangeUseCase(
                appointmentRepository,
                new AppointmentMapper()
        );
    }

    /**
     * Las fechas son inclusivas para quien llama, pero contra la base el rango
     * es semiabierto: incluye la medianoche del primer día y excluye la del día
     * siguiente al último, para que ningún turno quede afuera.
     */
    @Test
    void queriesTheRangeAsAHalfOpenInterval() {
        when(appointmentRepository
                .findAllByUserIdAndScheduledAtGreaterThanEqualAndScheduledAtLessThanOrderByScheduledAtAsc(
                        7L,
                        LocalDateTime.of(2026, 9, 7, 0, 0),
                        LocalDateTime.of(2026, 9, 14, 0, 0)
                ))
                .thenReturn(List.of(appointmentWith(AppointmentStatus.SCHEDULED)));

        var responses = useCase.execute(
                7L,
                LocalDate.of(2026, 9, 7),
                LocalDate.of(2026, 9, 13)
        );

        assertThat(responses).hasSize(1);
        assertThat(responses.getFirst().licensePlate()).isEqualTo("AB123CD");
        assertThat(responses.getFirst().customerLastName()).isEqualTo("Pérez");
    }

    /**
     * La vista diaria pide un rango de un solo día.
     */
    @Test
    void supportsASingleDayRange() {
        when(appointmentRepository
                .findAllByUserIdAndScheduledAtGreaterThanEqualAndScheduledAtLessThanOrderByScheduledAtAsc(
                        7L,
                        LocalDateTime.of(2026, 9, 10, 0, 0),
                        LocalDateTime.of(2026, 9, 11, 0, 0)
                ))
                .thenReturn(List.of());

        var date = LocalDate.of(2026, 9, 10);

        assertThat(useCase.execute(7L, date, date)).isEmpty();
    }

    @Test
    void acceptsRangeAtTheLimit() {
        LocalDate from = LocalDate.of(2026, 1, 1);
        LocalDate to = from.plusDays(ListAppointmentsByRangeUseCase.MAX_RANGE_DAYS - 1);

        when(appointmentRepository
                .findAllByUserIdAndScheduledAtGreaterThanEqualAndScheduledAtLessThanOrderByScheduledAtAsc(
                        anyLong(),
                        any(LocalDateTime.class),
                        any(LocalDateTime.class)
                ))
                .thenReturn(List.of());

        assertThat(useCase.execute(7L, from, to)).isEmpty();
    }

    @Test
    void rejectsRangeLongerThanTheLimit() {
        LocalDate from = LocalDate.of(2026, 1, 1);
        LocalDate to = from.plusDays(ListAppointmentsByRangeUseCase.MAX_RANGE_DAYS);

        assertThatThrownBy(() -> useCase.execute(7L, from, to))
                .isInstanceOf(InvalidDateRangeException.class)
                .hasMessageContaining("92");

        verifyNoQuery();
    }

    @Test
    void rejectsInvertedRange() {
        assertThatThrownBy(() -> useCase.execute(
                7L,
                LocalDate.of(2026, 9, 10),
                LocalDate.of(2026, 9, 9)
        ))
                .isInstanceOf(InvalidDateRangeException.class)
                .hasMessageContaining("posterior");

        verifyNoQuery();
    }

    private void verifyNoQuery() {
        verify(appointmentRepository, never())
                .findAllByUserIdAndScheduledAtGreaterThanEqualAndScheduledAtLessThanOrderByScheduledAtAsc(
                        anyLong(),
                        any(LocalDateTime.class),
                        any(LocalDateTime.class)
                );
    }
}
