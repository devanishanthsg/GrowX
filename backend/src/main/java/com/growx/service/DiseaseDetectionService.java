package com.growx.service;

import com.growx.dto.response.DiseaseDetectionResponse;
import com.growx.entity.DiseaseDetection;
import com.growx.entity.Farm;
import com.growx.exception.BadRequestException;
import com.growx.exception.ResourceNotFoundException;
import com.growx.mapper.DiseaseDetectionMapper;
import com.growx.repository.DiseaseDetectionRepository;
import com.growx.repository.FarmRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Orchestrates plant disease detection requests.
 *
 * Flow:
 * 1. Validates the farm belongs to the requesting user
 * 2. Stores the uploaded image via FileStorageService
 * 3. Calls the ML client (when connected)
 * 4. Persists the detection record
 * 5. Returns the response DTO
 *
 * The DiseaseMlClient is not yet connected — records are saved with null result fields
 * until the ML service is implemented.
 */
@Service
@RequiredArgsConstructor
public class DiseaseDetectionService {

    private final DiseaseDetectionRepository diseaseDetectionRepository;
    private final FarmRepository farmRepository;
    private final FileStorageService fileStorageService;
    private final DiseaseDetectionMapper mapper;

    // ML client is not yet wired — will be @Autowired when implemented
    // private final DiseaseMlClient diseaseMlClient;

    @Transactional
    public DiseaseDetectionResponse analyzeImage(MultipartFile image, Long farmId, Long userId) {
        // Validate file
        if (image == null || image.isEmpty()) {
            throw new BadRequestException("Please provide an image file.");
        }

        // Verify the farm belongs to the authenticated user
        Farm farm = farmRepository.findByIdAndOwnerId(farmId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Farm", farmId));

        // Store the uploaded image and get its path
        String imagePath = fileStorageService.storeImage(image, farmId);

        // Persist detection record with image path (ML results will be null until connected)
        DiseaseDetection entity = DiseaseDetection.builder()
                .imagePath(imagePath)
                .farm(farm)
                .build();

        entity = diseaseDetectionRepository.save(entity);

        // TODO: Call diseaseMlClient.detect(image) once ML service is connected
        // and update entity fields: diseaseName, confidence, severity, treatment, prevention
        // entity = diseaseDetectionRepository.save(entity);

        return mapper.toResponse(entity);
    }

    @Transactional(readOnly = true)
    public List<DiseaseDetectionResponse> getDetectionsForFarm(Long farmId, Long userId) {
        farmRepository.findByIdAndOwnerId(farmId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Farm", farmId));

        return diseaseDetectionRepository.findByFarmIdOrderByDetectedAtDesc(farmId)
                .stream()
                .map(mapper::toResponse)
                .toList();
    }
}
