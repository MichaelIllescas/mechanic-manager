package com.mechanicmanager.supplies.service;

import com.mechanicmanager.shared.exception.ResourceNotFoundException;
import com.mechanicmanager.supplies.dto.SupplyResponse;
import com.mechanicmanager.supplies.mapper.SupplyMapper;
import com.mechanicmanager.supplies.model.Supply;
import com.mechanicmanager.supplies.repository.SupplyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GetSupplyUseCase {

    private final SupplyRepository supplyRepository;
    private final SupplyMapper supplyMapper;

    @Transactional(readOnly = true)
    public SupplyResponse execute(Long id, Long userId) {
        Supply supply = supplyRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No se encontró el insumo con id " + id));

        return supplyMapper.toResponse(supply);
    }
}
