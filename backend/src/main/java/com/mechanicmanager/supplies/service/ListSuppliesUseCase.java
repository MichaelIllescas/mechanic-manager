package com.mechanicmanager.supplies.service;

import com.mechanicmanager.supplies.dto.SupplyResponse;
import com.mechanicmanager.supplies.mapper.SupplyMapper;
import com.mechanicmanager.supplies.repository.SupplyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ListSuppliesUseCase {

    private final SupplyRepository supplyRepository;
    private final SupplyMapper supplyMapper;

    /**
     * @param active null devuelve todo el catálogo; true o false lo filtra.
     *               La pantalla de administración lista todo, el alta de ítems
     *               en presupuestos y órdenes lista solo los activos.
     */
    @Transactional(readOnly = true)
    public List<SupplyResponse> execute(Long userId, Boolean active) {
        return (active == null
                ? supplyRepository.findByUserIdOrderByNameAsc(userId)
                : supplyRepository.findByUserIdAndActiveOrderByNameAsc(userId, active))
                .stream()
                .map(supplyMapper::toResponse)
                .toList();
    }
}
