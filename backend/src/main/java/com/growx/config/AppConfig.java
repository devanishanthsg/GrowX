package com.growx.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * General application configuration beans for GrowX.
 * Houses beans that don't naturally belong to a more specific config class.
 */
@Configuration
public class AppConfig {

    /**
     * File storage root directory for uploaded images.
     * Injected from properties — never hardcoded to a machine path.
     */
    @Value("${growx.storage.upload-dir:uploads}")
    private String uploadDir;

    /**
     * RestTemplate for making HTTP calls to external APIs
     * (weather service, ML services).
     * Individual client classes inject this bean.
     */
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
