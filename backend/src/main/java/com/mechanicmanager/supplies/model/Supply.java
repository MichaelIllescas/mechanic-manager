package com.mechanicmanager.supplies.model;

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
 * Insumo o repuesto que utiliza el taller: aceite, filtros, pastillas, bujías.
 * Es catálogo, no consumo realizado: no lleva existencias ni movimientos de stock,
 * que quedan fuera del MVP (ADR-010).
 */
@Entity
@Table(name = "supplies")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Supply {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @NotBlank(message = "El nombre del insumo es obligatorio")
    @Size(max = 120, message = "El nombre no puede superar los 120 caracteres")
    @Column(name = "name", nullable = false, length = 120)
    private String name;
    
    /**
     * Precio de referencia. Los presupuestos y las órdenes de trabajo copian
     * este valor al crear el ítem, por lo que modificarlo no altera lo ya registrado.
     */
    @NotNull(message = "El precio es obligatorio")
    @DecimalMin(value = "0.0", message = "El precio no puede ser negativo")
    @Column(name = "price", nullable = false, precision = 12, scale = 2)
    private BigDecimal price;
    
    /**
     * Un insumo ya utilizado no se elimina: se desactiva, para no romper
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
