package com.mechanicmanager.supplies.repository;

import com.mechanicmanager.supplies.model.Supply;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SupplyRepository extends JpaRepository<Supply, Long> {
    
    /**
     * Toda búsqueda incluye el userId: el catálogo de un usuario no debe
     * ser alcanzable desde la sesión de otro, aunque conozca el id.
     */
    Optional<Supply> findByIdAndUserId(Long id, Long userId);
    
    List<Supply> findByUserIdOrderByNameAsc(Long userId);
    
    List<Supply> findByUserIdAndActiveOrderByNameAsc(Long userId, boolean active);
    
    boolean existsByUserIdAndNameIgnoreCase(Long userId, String name);
    
    boolean existsByUserIdAndNameIgnoreCaseAndIdNot(Long userId, String name, Long id);
}
