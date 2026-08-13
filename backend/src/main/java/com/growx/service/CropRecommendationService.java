package com.growx.service;

import com.growx.client.CropMlClient;
import com.growx.dto.request.CropRecommendationRequest;
import com.growx.dto.response.CropRecommendationResponse;
import com.growx.entity.CropRecommendation;
import com.growx.entity.Farm;
import com.growx.enums.RecommendationStatus;
import com.growx.exception.ResourceNotFoundException;
import com.growx.mapper.CropRecommendationMapper;
import com.growx.repository.CropRecommendationRepository;
import com.growx.repository.FarmRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Orchestrates crop recommendation requests.
 *
 * Flow:
 * 1. Validates the farm belongs to the requesting user
 * 2. Persists the input parameters
 * 3. Calls the ML client (when connected)
 * 4. Updates the record with results and returns the response DTO
 *
 * The CropMlClient is not yet connected — results will be PENDING
 * until the ML service is implemented.
 */
@Service
@RequiredArgsConstructor
public class CropRecommendationService {

    private final CropRecommendationRepository cropRecommendationRepository;
    private final FarmRepository farmRepository;
    private final CropRecommendationMapper mapper;

    // ML client is not yet wired — will be @Autowired when implemented
    // private final CropMlClient cropMlClient;

    @Transactional
    public CropRecommendationResponse requestRecommendation(
            CropRecommendationRequest request,
            Long userId
    ) {
        // Verify the farm belongs to the authenticated user
        Farm farm = farmRepository.findByIdAndOwnerId(request.getFarmId(), userId)
                .orElseThrow(() -> new ResourceNotFoundException("Farm", request.getFarmId()));

        // Persist input parameters immediately (before calling ML)
        CropRecommendation entity = CropRecommendation.builder()
                .nitrogen(request.getNitrogen())
                .phosphorus(request.getPhosphorus())
                .potassium(request.getPotassium())
                .temperature(request.getTemperature())
                .humidity(request.getHumidity())
                .ph(request.getPh())
                .rainfall(request.getRainfall())
                .status(RecommendationStatus.PENDING)
                .farm(farm)
                .build();

        entity = cropRecommendationRepository.save(entity);

        // TODO: Call cropMlClient.predict(request) once ML service is connected
        // and update entity fields: recommendedCrop, confidence, season, expectedYield, explanation
        // entity.setStatus(RecommendationStatus.COMPLETED);
        // entity = cropRecommendationRepository.save(entity);

        return mapper.toResponse(entity);
    }

    @Transactional(readOnly = true)
    public List<CropRecommendationResponse> getRecommendationsForFarm(Long farmId, Long userId) {
        // Verify ownership before returning data
        farmRepository.findByIdAndOwnerId(farmId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Farm", farmId));

        return cropRecommendationRepository.findByFarmIdOrderByCreatedAtDesc(farmId)
                .stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public CropRecommendationResponse getRecommendationById(Long id, Long userId) {
        CropRecommendation entity = cropRecommendationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("CropRecommendation", id));

        // Verify ownership
        farmRepository.findByIdAndOwnerId(entity.getFarm().getId(), userId)
                .orElseThrow(() -> new ResourceNotFoundException("CropRecommendation", id));

        return mapper.toResponse(entity);
    }
}
