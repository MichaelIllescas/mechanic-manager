package com.mechanicmanager.servicetypes.service;

import com.mechanicmanager.servicetypes.dto.ServiceTypeResponse;
import com.mechanicmanager.servicetypes.mapper.ServiceTypeMapper;
import com.mechanicmanager.servicetypes.repository.ServiceTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ListServiceTypesUseCase {

    private final ServiceTypeRepository serviceTypeRepository;
    private final ServiceTypeMapper serviceTypeMapper;

    /**
     * @param active null devuelve todo el catálogo; true o false lo filtra.
     *               La pantalla de administración lista todo, el alta de ítems
     *               en presupuestos y órdenes lista solo los activos.
     */
    @Transactional(readOnly = true)
    public List<ServiceTypeResponse> execute(Long userId, Boolean active) {
        return (active == null
                ? serviceTypeRepository.findByUserIdOrderByNameAsc(userId)
                : serviceTypeRepository.findByUserIdAndActiveOrderByNameAsc(userId, active))
                .stream()
                .map(serviceTypeMapper::toResponse)
                .toList();
    }
}
