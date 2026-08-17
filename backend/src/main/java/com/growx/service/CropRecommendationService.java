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

        // Keep the complete immediate AI response.
        //
        // V2 reliability metadata is returned to React but is intentionally
        // not persisted yet. Durable DB fields continue to use the existing
        // CropRecommendation entity.

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
            //
            // IMPORTANT:
            // For LOW_CONFIDENCE / UNCERTAIN / OUT_OF_DISTRIBUTION,
            // recommendedCrop is intentionally null. We do NOT persist the
            // raw topCandidate as if it were an approved recommendation.
            // ------------------------------------------------

            entity.setRecommendedCrop(
                    Boolean.TRUE.equals(
                            prediction.getRecommendationAvailable()
                    )
                            ? prediction.getRecommendedCrop()
                            : null
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
        // Add all V2 reliability metadata to immediate response
        // ------------------------------------------------

        if (prediction != null) {

            // Recommendation gate
            response.setRecommendationAvailable(
                    prediction.getRecommendationAvailable()
            );

            response.setRecommendationMessage(
                    prediction.getRecommendationMessage()
            );

            // Ranked candidates
            response.setTopCandidate(
                    prediction.getTopCandidate()
            );

            response.setCandidates(
                    prediction.getCandidates()
            );

            response.setAlternatives(
                    prediction.getAlternatives()
            );

            // Confidence / reliability
            response.setConfidenceMargin(
                    prediction.getConfidenceMargin()
            );

            response.setReliabilityStatus(
                    prediction.getReliabilityStatus()
            );

            response.setReliabilityMessage(
                    prediction.getReliabilityMessage()
            );

            // Input-domain metadata
            response.setDomainStatus(
                    prediction.getDomainStatus()
            );

            response.setDomainScore(
                    prediction.getDomainScore()
            );

            response.setDomainOutsideFeatures(
                    prediction.getDomainOutsideFeatures()
            );

            // Season
            response.setSeasonStatus(
                    prediction.getSeasonStatus()
            );

            response.setSeasonMessage(
                    prediction.getSeasonMessage()
            );

            // Yield
            response.setYieldStatus(
                    prediction.getYieldStatus()
            );

            response.setYieldMessage(
                    prediction.getYieldMessage()
            );

            // Model
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
