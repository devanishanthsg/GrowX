package com.growx.client.impl;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.growx.client.CropMlClient;
import com.growx.dto.request.CropRecommendationRequest;
import com.growx.dto.response.CropRecommendationResponse;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class CropMlClientImpl implements CropMlClient {

    private final RestTemplate restTemplate;

    @Value("${growx.ml.crop-url}")
    private String cropMlUrl;

    @Override
    public CropRecommendationResponse predict(
            CropRecommendationRequest request,
            String district
    ) {

        Map<String, Object> body = new HashMap<>();

        // ML input values
        body.put("N", request.getNitrogen());
        body.put("P", request.getPhosphorus());
        body.put("K", request.getPotassium());
        body.put("temperature", request.getTemperature());
        body.put("humidity", request.getHumidity());
        body.put("ph", request.getPh());
        body.put("rainfall", request.getRainfall());

        // Season/yield engine inputs
        body.put("district", district);
        body.put("sowingMonth", request.getSowingMonth());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> httpEntity =
                new HttpEntity<>(body, headers);

        try {

            ResponseEntity<MlResponse> response =
                    restTemplate.exchange(
                            cropMlUrl,
                            HttpMethod.POST,
                            httpEntity,
                            MlResponse.class
                    );

            MlResponse ml = response.getBody();

            if (ml == null) {
                throw new IllegalStateException(
                        "AI service returned an empty response"
                );
            }

            if ("INVALID_INPUT".equalsIgnoreCase(ml.getStatus())) {
                throw new IllegalArgumentException(
                        "AI rejected the provided crop parameters"
                );
            }

            return CropRecommendationResponse
                    .builder()

                    // Recommendation gate
                    .recommendationAvailable(
                            ml.getRecommendationAvailable()
                    )

                    .recommendationMessage(
                            ml.getRecommendationMessage()
                    )

                    .recommendedCrop(
                            ml.getRecommendedCrop()
                    )

                    .topCandidate(
                            toCandidate(ml.getTopCandidate())
                    )

                    .candidates(
                            toCandidates(ml.getCandidates())
                    )

                    .alternatives(
                            toCandidates(ml.getAlternatives())
                    )

                    .confidence(
                            toBigDecimal(ml.getConfidence())
                    )

                    .confidenceMargin(
                            toBigDecimal(ml.getConfidenceMargin())
                    )

                    // Reliability / domain
                    .reliabilityStatus(
                            ml.getReliabilityStatus()
                    )

                    .reliabilityMessage(
                            ml.getReliabilityMessage()
                    )

                    .domainStatus(
                            ml.getDomainStatus()
                    )

                    .domainScore(
                            toBigDecimal(ml.getDomainScore())
                    )

                    .domainOutsideFeatures(
                            ml.getDomainOutsideFeatures()
                    )

                    // Season
                    .season(
                            ml.getSeason()
                    )

                    .seasonStatus(
                            ml.getSeasonStatus()
                    )

                    .seasonMessage(
                            ml.getSeasonMessage()
                    )

                    // Yield
                    .expectedYield(
                            ml.getExpectedYield()
                    )

                    .yieldStatus(
                            ml.getYieldStatus()
                    )

                    .yieldMessage(
                            ml.getYieldMessage()
                    )

                    // Explanation
                    .explanation(
                            ml.getExplanation()
                    )

                    // Model version
                    .modelVersion(
                            ml.getModelVersion()
                    )

                    .build();

        } catch (RestClientException ex) {

            throw new IllegalStateException(
                    "Unable to connect to GrowX AI service",
                    ex
            );
        }
    }

    private BigDecimal toBigDecimal(Double value) {
        return value == null
                ? null
                : BigDecimal.valueOf(value);
    }

    private CropRecommendationResponse.CropCandidate toCandidate(
            Candidate candidate
    ) {
        if (candidate == null) {
            return null;
        }

        return CropRecommendationResponse.CropCandidate
                .builder()
                .crop(candidate.getCrop())
                .confidence(toBigDecimal(candidate.getConfidence()))
                .build();
    }

    private List<CropRecommendationResponse.CropCandidate> toCandidates(
            List<Candidate> candidates
    ) {
        if (candidates == null) {
            return null;
        }

        return candidates
                .stream()
                .map(this::toCandidate)
                .toList();
    }

    // ====================================================
    // FastAPI V2 response structure
    // ====================================================

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class MlResponse {

        private String status;

        // Recommendation gate
        private Boolean recommendationAvailable;
        private String recommendationMessage;
        private String recommendedCrop;

        // Ranked candidates
        private Candidate topCandidate;
        private List<Candidate> candidates;
        private List<Candidate> alternatives;

        // Model confidence
        private Double confidence;
        private Double confidenceMargin;

        // Reliability / domain
        private String reliabilityStatus;
        private String reliabilityMessage;
        private String domainStatus;
        private Double domainScore;
        private List<String> domainOutsideFeatures;

        // Season
        private String season;
        private String seasonStatus;
        private String seasonMessage;

        // Yield
        private String expectedYield;
        private String yieldStatus;
        private String yieldMessage;

        // Explanation
        private String explanation;

        // Model metadata
        private String modelVersion;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class Candidate {
        private String crop;
        private Double confidence;
    }
}
