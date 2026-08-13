package com.growx.service;

import com.growx.dto.request.CreateFarmRequest;
import com.growx.dto.request.UpdateFarmRequest;
import com.growx.dto.response.FarmResponse;
import com.growx.entity.Farm;
import com.growx.entity.User;
import com.growx.exception.ResourceNotFoundException;
import com.growx.exception.UnauthorizedException;
import com.growx.mapper.FarmMapper;
import com.growx.repository.FarmRepository;
import com.growx.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * Manages farm CRUD operations.
 * All operations are scoped to the authenticated user (data isolation).
 */
@Service
@RequiredArgsConstructor
public class FarmService {

    private final FarmRepository farmRepository;
    private final UserRepository userRepository;
    private final FarmMapper farmMapper;

    @Transactional(readOnly = true)
    public List<FarmResponse> getFarmsForUser(Long userId) {
        return farmRepository.findByOwnerId(userId)
                .stream()
                .map(farmMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public FarmResponse getFarmById(Long farmId, Long userId) {
        Farm farm = farmRepository.findByIdAndOwnerId(farmId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Farm", farmId));
        return farmMapper.toResponse(farm);
    }

    @Transactional
    public FarmResponse createFarm(CreateFarmRequest request, Long userId) {
        User owner = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        Farm farm = Farm.builder()
                .farmName(request.getFarmName())
                .location(request.getLocation())
                .latitude(request.getLatitude() != null ? BigDecimal.valueOf(request.getLatitude()) : null)
                .longitude(request.getLongitude() != null ? BigDecimal.valueOf(request.getLongitude()) : null)
                .area(request.getArea() != null ? BigDecimal.valueOf(request.getArea()) : null)
                .areaUnit(request.getAreaUnit() != null ? request.getAreaUnit() : "acres")
                .soilType(request.getSoilType())
                .mainCrop(request.getMainCrop())
                .owner(owner)
                .build();

        return farmMapper.toResponse(farmRepository.save(farm));
    }

    @Transactional
    public FarmResponse updateFarm(Long farmId, UpdateFarmRequest request, Long userId) {
        Farm farm = farmRepository.findByIdAndOwnerId(farmId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Farm", farmId));

        if (request.getFarmName() != null) farm.setFarmName(request.getFarmName());
        if (request.getLocation() != null) farm.setLocation(request.getLocation());
        if (request.getLatitude() != null) farm.setLatitude(BigDecimal.valueOf(request.getLatitude()));
        if (request.getLongitude() != null) farm.setLongitude(BigDecimal.valueOf(request.getLongitude()));
        if (request.getArea() != null) farm.setArea(BigDecimal.valueOf(request.getArea()));
        if (request.getAreaUnit() != null) farm.setAreaUnit(request.getAreaUnit());
        if (request.getSoilType() != null) farm.setSoilType(request.getSoilType());
        if (request.getMainCrop() != null) farm.setMainCrop(request.getMainCrop());

        return farmMapper.toResponse(farmRepository.save(farm));
    }

    @Transactional
    public void deleteFarm(Long farmId, Long userId) {
        if (!farmRepository.existsByIdAndOwnerId(farmId, userId)) {
            throw new ResourceNotFoundException("Farm", farmId);
        }
        farmRepository.deleteById(farmId);
    }
}
