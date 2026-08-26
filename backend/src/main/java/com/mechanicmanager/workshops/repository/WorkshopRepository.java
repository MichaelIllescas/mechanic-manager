package com.mechanicmanager.workshops.repository;

import com.mechanicmanager.workshops.model.Workshop;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WorkshopRepository extends JpaRepository<Workshop, Long> {
    
    Optional<Workshop> findByUserId(Long userId);
    
    boolean existsByUserId(Long userId);
}
