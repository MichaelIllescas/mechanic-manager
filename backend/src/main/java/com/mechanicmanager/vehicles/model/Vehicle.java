package com.mechanicmanager.vehicles.model;

import com.mechanicmanager.customers.model.Customer;
import com.mechanicmanager.users.model.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Generated;
import org.hibernate.generator.EventType;

import java.time.LocalDateTime;

@Entity
@Table(name = "vehicles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Vehicle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @Column(name = "license_plate", nullable = false, length = 20)
    private String licensePlate;

    @Column(name = "brand", nullable = false, length = 80)
    private String brand;

    @Column(name = "model", nullable = false, length = 120)
    private String model;

    @Column(name = "year")
    private Integer year;

    @Column(name = "chassis_number", nullable = false, length = 100)
    private String chassisNumber;

    @Column(name = "engine_number", nullable = false, length = 100)
    private String engineNumber;

    @Column(name = "mileage")
    private Integer mileage;

    @Enumerated(EnumType.STRING)
    @Column(name = "vehicle_type", nullable = false, length = 40)
    private VehicleType vehicleType;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Generated(event = EventType.INSERT)
    @Column(name = "created_at", nullable = false, insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Generated(event = {EventType.INSERT, EventType.UPDATE})
    @Column(name = "updated_at", nullable = false, insertable = false, updatable = false)
    private LocalDateTime updatedAt;
}
