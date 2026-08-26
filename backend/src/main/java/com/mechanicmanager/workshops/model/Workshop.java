package com.mechanicmanager.workshops.model;

import com.mechanicmanager.users.model.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Datos del taller. Se crea vacío junto con el usuario y este lo va completando,
 * por eso ningún campo de negocio es obligatorio: solo se valida el formato.
 */
@Entity
@Table(name = "workshops")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Workshop {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;
    
    @Size(max = 120, message = "El nombre no puede superar los 120 caracteres")
    @Column(name = "name", length = 120)
    private String name;
    
    @Pattern(regexp = "^\\s*$|^[0-9+()\\-\\s]{6,30}$",
            message = "El teléfono solo puede contener números, espacios y los símbolos + - ( )")
    @Column(name = "phone", length = 30)
    private String phone;
    
    @Email(message = "El email no tiene un formato válido")
    @Size(max = 255, message = "El email no puede superar los 255 caracteres")
    @Column(name = "email", length = 255)
    private String email;
    
    @Size(max = 255, message = "La dirección no puede superar los 255 caracteres")
    @Column(name = "address", length = 255)
    private String address;
    
    @Size(max = 1000, message = "Las notas no pueden superar los 1000 caracteres")
    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;
    
    @Column(name = "created_at", nullable = false, insertable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at", nullable = false, insertable = false, updatable = false)
    private LocalDateTime updatedAt;
    
    /**
     * Indica si el taller tiene cargados los datos mínimos para poder emitir
     * documentos (presupuestos, órdenes de trabajo) con encabezado válido.
     */
    public boolean isComplete() {
        return hasText(name) && hasText(phone) && hasText(address);
    }
    
    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
