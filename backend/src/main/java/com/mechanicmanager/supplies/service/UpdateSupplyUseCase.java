package com.mechanicmanager.supplies.service;

import com.mechanicmanager.shared.exception.ResourceNotFoundException;
import com.mechanicmanager.supplies.dto.SupplyResponse;
import com.mechanicmanager.supplies.dto.UpdateSupplyRequest;
import com.mechanicmanager.supplies.exception.SupplyNameAlreadyExistsException;
import com.mechanicmanager.supplies.mapper.SupplyMapper;
import com.mechanicmanager.supplies.model.Supply;
import com.mechanicmanager.supplies.repository.SupplyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UpdateSupplyUseCase {

    private final SupplyRepository supplyRepository;
    private final SupplyMapper supplyMapper;

    @Transactional
    public SupplyResponse execute(Long id, UpdateSupplyRequest request, Long userId) {
        Supply supply = supplyRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No se encontró el insumo con id " + id));

        String name = request.name().trim();

        if (supplyRepository.existsByUserIdAndNameIgnoreCaseAndIdNot(userId, name, id)) {
            throw new SupplyNameAlreadyExistsException(name);
        }

        // Cambiar el precio no afecta presupuestos ni órdenes ya registrados:
        // esos copian el valor al crear el ítem (ADR-009, ADR-010).
        supply.setName(name);
        supply.setPrice(request.price());

        return supplyMapper.toResponse(supplyRepository.save(supply));
    }
}
