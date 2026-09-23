package com.mechanicmanager.servicetypes.model;

import com.mechanicmanager.users.model.User;
import jakarta.persistence.*;
import org.hibernate.annotations.Generated;
import org.hibernate.generator.EventType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Servicio que ofrece el taller: cambio de aceite, alineación, cambio de pastillas.
 * Es catálogo, no trabajo realizado: no tiene fecha, estado ni vehículo asociado.
 * El trabajo efectivamente ejecutado se registra en las órdenes de trabajo (ADR-009).
 */
@Entity
@Table(name = "service_types")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ServiceType {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @NotBlank(message = "El nombre del servicio es obligatorio")
    @Size(max = 120, message = "El nombre no puede superar los 120 caracteres")
    @Column(name = "name", nullable = false, length = 120)
    private String name;
    
    @Size(max = 500, message = "La descripción no puede superar los 500 caracteres")
    @Column(name = "description", length = 500)
    private String description;
    
    /**
     * Precio de referencia. Los presupuestos y las órdenes de trabajo copian
     * este valor al crear el ítem, por lo que modificarlo no altera lo ya registrado.
     */
    @NotNull(message = "El precio es obligatorio")
    @DecimalMin(value = "0.0", message = "El precio no puede ser negativo")
    @Column(name = "base_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal basePrice;
    
    /**
     * Un servicio ya utilizado no se elimina: se desactiva, para no romper
     * el historial de presupuestos y órdenes que lo referencian.
     */
    @Builder.Default
    @Column(name = "active", nullable = false)
    private boolean active = true;
    
    /**
     * Lo genera la base con su DEFAULT. @Generated hace que Hibernate lo relea
     * después del INSERT: sin eso, la entidad recién guardada queda con null
     * y la response del POST devuelve null.
     */
    @Generated(event = EventType.INSERT)
    @Column(name = "created_at", nullable = false, insertable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Generated(event = {EventType.INSERT, EventType.UPDATE})
    @Column(name = "updated_at", nullable = false, insertable = false, updatable = false)
    private LocalDateTime updatedAt;
}
