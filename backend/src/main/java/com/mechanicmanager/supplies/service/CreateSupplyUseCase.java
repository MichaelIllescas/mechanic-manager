package com.mechanicmanager.supplies.service;

import com.mechanicmanager.supplies.dto.CreateSupplyRequest;
import com.mechanicmanager.supplies.dto.SupplyResponse;
import com.mechanicmanager.supplies.exception.SupplyNameAlreadyExistsException;
import com.mechanicmanager.supplies.mapper.SupplyMapper;
import com.mechanicmanager.supplies.model.Supply;
import com.mechanicmanager.supplies.repository.SupplyRepository;
import com.mechanicmanager.users.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CreateSupplyUseCase {

    private final SupplyRepository supplyRepository;
    private final UserRepository userRepository;
    private final SupplyMapper supplyMapper;

    @Transactional
    public SupplyResponse execute(CreateSupplyRequest request, Long userId) {
        String name = request.name().trim();

        if (supplyRepository.existsByUserIdAndNameIgnoreCase(userId, name)) {
            throw new SupplyNameAlreadyExistsException(name);
        }

        Supply supply = Supply.builder()
                .user(userRepository.getReferenceById(userId))
                .name(name)
                .price(request.price())
                .active(true)
                .build();

        return supplyMapper.toResponse(supplyRepository.save(supply));
    }
}
