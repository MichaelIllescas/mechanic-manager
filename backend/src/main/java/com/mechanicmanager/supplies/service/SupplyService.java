package com.mechanicmanager.supplies.service;

import com.mechanicmanager.supplies.dto.CreateSupplyRequest;
import com.mechanicmanager.supplies.dto.SupplyResponse;
import com.mechanicmanager.supplies.dto.UpdateSupplyRequest;
import com.mechanicmanager.supplies.dto.UpdateSupplyStatusRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SupplyService {

    private final CreateSupplyUseCase createSupplyUseCase;
    private final UpdateSupplyUseCase updateSupplyUseCase;
    private final UpdateSupplyStatusUseCase updateSupplyStatusUseCase;
    private final GetSupplyUseCase getSupplyUseCase;
    private final ListSuppliesUseCase listSuppliesUseCase;

    public SupplyResponse create(CreateSupplyRequest request, Long userId) {
        return createSupplyUseCase.execute(request, userId);
    }

    public SupplyResponse update(Long id, UpdateSupplyRequest request, Long userId) {
        return updateSupplyUseCase.execute(id, request, userId);
    }

    public SupplyResponse updateStatus(Long id, UpdateSupplyStatusRequest request, Long userId) {
        return updateSupplyStatusUseCase.execute(id, request, userId);
    }

    public SupplyResponse findById(Long id, Long userId) {
        return getSupplyUseCase.execute(id, userId);
    }

    public List<SupplyResponse> findAll(Long userId, Boolean active) {
        return listSuppliesUseCase.execute(userId, active);
    }
}
