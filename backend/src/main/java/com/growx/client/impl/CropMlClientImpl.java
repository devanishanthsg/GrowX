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

                    .recommendedCrop(
                            ml.getRecommendedCrop()
                    )

                    .confidence(
                            ml.getConfidence() == null
                                    ? null
                                    : BigDecimal.valueOf(
                                            ml.getConfidence()
                                    )
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

    // ====================================================
    // FastAPI response structure
    // ====================================================

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class MlResponse {

        private String status;

        private String recommendedCrop;

        private Double confidence;

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

        // Alternative predictions
        private List<Alternative> alternatives;

        // Model metadata
        private String modelVersion;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class Alternative {

        private String crop;

        private Double confidence;
    }
}