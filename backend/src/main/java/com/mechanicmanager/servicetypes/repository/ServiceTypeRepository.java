package com.mechanicmanager.servicetypes.repository;

import com.mechanicmanager.servicetypes.model.ServiceType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ServiceTypeRepository extends JpaRepository<ServiceType, Long> {
    
    /**
     * Toda búsqueda incluye el userId: el catálogo de un usuario no debe
     * ser alcanzable desde la sesión de otro, aunque conozca el id.
     */
    Optional<ServiceType> findByIdAndUserId(Long id, Long userId);
    
    List<ServiceType> findByUserIdOrderByNameAsc(Long userId);
    
    List<ServiceType> findByUserIdAndActiveOrderByNameAsc(Long userId, boolean active);
    
    boolean existsByUserIdAndNameIgnoreCase(Long userId, String name);
    
    boolean existsByUserIdAndNameIgnoreCaseAndIdNot(Long userId, String name, Long id);
}
