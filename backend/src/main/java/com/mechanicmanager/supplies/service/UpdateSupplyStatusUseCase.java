package com.mechanicmanager.supplies.service;

import com.mechanicmanager.shared.exception.ResourceNotFoundException;
import com.mechanicmanager.supplies.dto.SupplyResponse;
import com.mechanicmanager.supplies.dto.UpdateSupplyStatusRequest;
import com.mechanicmanager.supplies.mapper.SupplyMapper;
import com.mechanicmanager.supplies.model.Supply;
import com.mechanicmanager.supplies.repository.SupplyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Reemplaza al borrado: un insumo utilizado no se elimina, se desactiva,
 * para no romper el historial que lo referencia.
 */
@Service
@RequiredArgsConstructor
public class UpdateSupplyStatusUseCase {

    private final SupplyRepository supplyRepository;
    private final SupplyMapper supplyMapper;

    @Transactional
    public SupplyResponse execute(Long id, UpdateSupplyStatusRequest request, Long userId) {
        Supply supply = supplyRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No se encontró el insumo con id " + id));

        supply.setActive(request.active());

        return supplyMapper.toResponse(supplyRepository.save(supply));
    }
}
