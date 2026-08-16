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

@Service
@RequiredArgsConstructor
public class CropRecommendationService {

    private final CropRecommendationRepository cropRecommendationRepository;

    private final FarmRepository farmRepository;

    private final CropRecommendationMapper mapper;

    private final CropMlClient cropMlClient;


    @Transactional
    public CropRecommendationResponse requestRecommendation(
            CropRecommendationRequest request,
            Long userId
    ) {

        // ------------------------------------------------
        // Verify farm ownership
        // ------------------------------------------------

        Farm farm = farmRepository
                .findByIdAndOwnerId(
                        request.getFarmId(),
                        userId
                )
                .orElseThrow(
                        () -> new ResourceNotFoundException(
                                "Farm",
                                request.getFarmId()
                        )
                );


        // ------------------------------------------------
        // Extract district from farm location
        //
        // Example:
        // "Coimbatore, Tamil Nadu"
        // becomes:
        // "Coimbatore"
        // ------------------------------------------------

        String district = extractDistrict(
                farm.getLocation()
        );


        // ------------------------------------------------
        // Store recommendation request first
        // ------------------------------------------------

        CropRecommendation entity =
                CropRecommendation.builder()

                        .nitrogen(
                                request.getNitrogen()
                        )

                        .phosphorus(
                                request.getPhosphorus()
                        )

                        .potassium(
                                request.getPotassium()
                        )

                        .temperature(
                                request.getTemperature()
                        )

                        .humidity(
                                request.getHumidity()
                        )

                        .ph(
                                request.getPh()
                        )

                        .rainfall(
                                request.getRainfall()
                        )

                        .status(
                                RecommendationStatus.PENDING
                        )

                        .farm(farm)

                        .build();


        entity =
                cropRecommendationRepository.save(
                        entity
                );


        // ------------------------------------------------
        // This keeps the full immediate AI response.
        //
        // Some AI metadata such as seasonStatus,
        // yieldStatus and modelVersion are not stored
        // in the database yet.
        // ------------------------------------------------

        CropRecommendationResponse prediction = null;


        try {

            // ------------------------------------------------
            // Call Python AI service
            // ------------------------------------------------

            prediction =
                    cropMlClient.predict(
                            request,
                            district
                    );


            // ------------------------------------------------
            // Store durable AI result fields in MySQL
            // ------------------------------------------------

            entity.setRecommendedCrop(
                    prediction.getRecommendedCrop()
            );

            entity.setConfidence(
                    prediction.getConfidence()
            );

            entity.setSeason(
                    prediction.getSeason()
            );

            entity.setExpectedYield(
                    prediction.getExpectedYield()
            );

            entity.setExplanation(
                    prediction.getExplanation()
            );

            entity.setStatus(
                    RecommendationStatus.COMPLETED
            );


            entity =
                    cropRecommendationRepository.save(
                            entity
                    );


        } catch (Exception ex) {

            // ------------------------------------------------
            // If AI processing fails, keep the request record
            // but mark it as FAILED.
            // ------------------------------------------------

            entity.setStatus(
                    RecommendationStatus.FAILED
            );

            cropRecommendationRepository.save(
                    entity
            );

            throw ex;
        }


        // ------------------------------------------------
        // Build normal response from persisted entity
        // ------------------------------------------------

        CropRecommendationResponse response =
                mapper.toResponse(
                        entity
                );


        // ------------------------------------------------
        // Add AI reliability metadata to immediate response
        //
        // These fields are returned to React but are not
        // persisted in MySQL yet.
        // ------------------------------------------------

        if (prediction != null) {

            response.setSeasonStatus(
                    prediction.getSeasonStatus()
            );

            response.setSeasonMessage(
                    prediction.getSeasonMessage()
            );

            response.setYieldStatus(
                    prediction.getYieldStatus()
            );

            response.setYieldMessage(
                    prediction.getYieldMessage()
            );

            response.setModelVersion(
                    prediction.getModelVersion()
            );
        }


        return response;
    }


    // ====================================================
    // Recommendation history for farm
    // ====================================================

    @Transactional(readOnly = true)
    public List<CropRecommendationResponse>
    getRecommendationsForFarm(
            Long farmId,
            Long userId
    ) {

        // Verify farm ownership

        farmRepository
                .findByIdAndOwnerId(
                        farmId,
                        userId
                )
                .orElseThrow(
                        () -> new ResourceNotFoundException(
                                "Farm",
                                farmId
                        )
                );


        return cropRecommendationRepository
                .findByFarmIdOrderByCreatedAtDesc(
                        farmId
                )
                .stream()
                .map(
                        mapper::toResponse
                )
                .toList();
    }


    // ====================================================
    // Get single recommendation
    // ====================================================

    @Transactional(readOnly = true)
    public CropRecommendationResponse
    getRecommendationById(
            Long id,
            Long userId
    ) {

        CropRecommendation entity =
                cropRecommendationRepository
                        .findById(
                                id
                        )
                        .orElseThrow(
                                () ->
                                        new ResourceNotFoundException(
                                                "CropRecommendation",
                                                id
                                        )
                        );


        // Verify recommendation belongs to
        // one of the authenticated user's farms.

        farmRepository
                .findByIdAndOwnerId(
                        entity.getFarm().getId(),
                        userId
                )
                .orElseThrow(
                        () ->
                                new ResourceNotFoundException(
                                        "CropRecommendation",
                                        id
                                )
                );


        return mapper.toResponse(
                entity
        );
    }


    // ====================================================
    // Extract district from location
    // ====================================================

    private String extractDistrict(
            String location
    ) {

        if (
                location == null
                || location.isBlank()
        ) {
            return null;
        }


        String[] parts =
                location.split(",");


        return parts[0]
                .trim();
    }
}