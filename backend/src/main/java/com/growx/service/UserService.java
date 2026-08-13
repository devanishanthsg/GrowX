package com.growx.service;

import com.growx.dto.request.UpdateProfileRequest;
import com.growx.dto.response.UserResponse;
import com.growx.entity.Farm;
import com.growx.entity.User;
import com.growx.exception.ResourceNotFoundException;
import com.growx.mapper.UserMapper;
import com.growx.repository.FarmRepository;
import com.growx.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * Manages GrowX user profile operations.
 * Profile updates include both personal info (on User) and
 * primary farm info (on the user's first Farm), reflecting the Profile page UI.
 */
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final FarmRepository farmRepository;
    private final UserMapper userMapper;

    @Transactional(readOnly = true)
    public UserResponse getUserById(Long userId) {
        User user = findUserOrThrow(userId);
        return userMapper.toResponse(user);
    }

    @Transactional
    public UserResponse updateProfile(Long userId, UpdateProfileRequest request) {
        User user = findUserOrThrow(userId);

        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user = userRepository.save(user);

        // Update the user's primary farm if farm fields are provided
        List<Farm> farms = farmRepository.findByOwnerId(userId);
        if (!farms.isEmpty()) {
            Farm primaryFarm = farms.get(0);
            if (request.getFarmName() != null) {
                primaryFarm.setFarmName(request.getFarmName());
            }
            if (request.getLocation() != null) {
                primaryFarm.setLocation(request.getLocation());
            }
            if (request.getMainCrop() != null) {
                primaryFarm.setMainCrop(request.getMainCrop());
            }
            if (request.getLandArea() != null) {
                primaryFarm.setArea(BigDecimal.valueOf(request.getLandArea()));
                primaryFarm.setAreaUnit("acres");
            }
            farmRepository.save(primaryFarm);
        }

        return userMapper.toResponse(user);
    }

    private User findUserOrThrow(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));
    }
}
