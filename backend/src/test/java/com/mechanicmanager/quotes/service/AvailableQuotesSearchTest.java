package com.mechanicmanager.quotes.service;

import com.mechanicmanager.IntegrationTestBase;
import com.mechanicmanager.appointments.model.Appointment;
import com.mechanicmanager.appointments.model.AppointmentStatus;
import com.mechanicmanager.customers.model.Customer;
import com.mechanicmanager.quotes.dto.QuoteSummaryResponse;
import com.mechanicmanager.quotes.model.Quote;
import com.mechanicmanager.quotes.model.QuoteStatus;
import com.mechanicmanager.users.model.Role;
import com.mechanicmanager.users.model.User;
import com.mechanicmanager.users.model.UserStatus;
import com.mechanicmanager.vehicles.model.Vehicle;
import com.mechanicmanager.vehicles.model.VehicleType;
import com.mechanicmanager.workorders.model.WorkOrder;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * El filtro de disponibles se arma con subconsultas, así que se prueba contra
 * MySQL real: un mock del repositorio no diría nada sobre el SQL generado.
 */
@Transactional
class AvailableQuotesSearchTest extends IntegrationTestBase {

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private SearchQuotesUseCase searchQuotes;

    private User user;
    private Customer customer;
    private long nextNumber = 1;

    @Test
    void listsOnlyApprovedQuotesOfTheVehicleWithoutWorkOrderOrActiveAppointment() {
        user = persist(User.builder().name("Taller").email("available@test.local")
                .passwordHash("x").role(Role.USER).status(UserStatus.ENABLED).build());
        customer = persist(Customer.builder().user(user)
                .firstName("Ana").lastName("Gómez").phone("1").build());
        Vehicle vehicle = vehicle("AAA111");
        Vehicle otherVehicle = vehicle("BBB222");

        Quote free = quote(vehicle, QuoteStatus.APPROVED);
        Quote onlyCancelled = quote(vehicle, QuoteStatus.APPROVED);
        appointment(vehicle, onlyCancelled, AppointmentStatus.CANCELLED);
        Quote scheduled = quote(vehicle, QuoteStatus.APPROVED);
        appointment(vehicle, scheduled, AppointmentStatus.SCHEDULED);
        Quote worked = quote(vehicle, QuoteStatus.APPROVED);
        persist(WorkOrder.builder().user(user).vehicle(vehicle).quote(worked)
                .performedAt(LocalDate.now()).build());
        quote(vehicle, QuoteStatus.PENDING);
        quote(otherVehicle, QuoteStatus.APPROVED);

        entityManager.flush();
        entityManager.clear();

        var page = searchQuotes.execute(user.getId(), null, null, vehicle.getId(), true, 0, 20);

        assertThat(page.content())
                .extracting(QuoteSummaryResponse::id)
                .containsExactlyInAnyOrder(free.getId(), onlyCancelled.getId());
        assertThat(page.totalElements()).isEqualTo(2);
    }

    private Vehicle vehicle(String licensePlate) {
        return persist(Vehicle.builder().user(user).customer(customer)
                .licensePlate(licensePlate).brand("Peugeot").model("207")
                .vehicleType(VehicleType.CAR).build());
    }

    private Quote quote(Vehicle vehicle, QuoteStatus status) {
        return persist(Quote.builder().user(user).customer(customer).vehicle(vehicle)
                .number(nextNumber++).status(status).build());
    }

    private void appointment(Vehicle vehicle, Quote quote, AppointmentStatus status) {
        persist(Appointment.builder().user(user).vehicle(vehicle).quote(quote)
                .scheduledAt(LocalDateTime.now()).status(status).build());
    }

    private <T> T persist(T entity) {
        entityManager.persist(entity);
        return entity;
    }
}
