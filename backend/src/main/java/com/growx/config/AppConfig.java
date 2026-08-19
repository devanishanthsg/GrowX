package com.growx.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

@Configuration
public class AppConfig {

    /**
     * Primary RestTemplate retained for existing ML clients.
     * Keeping this bean primary avoids changing the working Crop Recommendation integration.
     */
    @Bean
    @Primary
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    /**
     * Weather-specific HTTP client with bounded connection/read timeouts.
     * External weather calls must never wait indefinitely.
     */
    @Bean
    @Qualifier("weatherRestTemplate")
    public RestTemplate weatherRestTemplate(
            RestTemplateBuilder builder,
            @Value("${growx.weather.connection-timeout-ms:3000}") long connectionTimeoutMs,
            @Value("${growx.weather.read-timeout-ms:5000}") long readTimeoutMs
    ) {
        return builder
                .setConnectTimeout(Duration.ofMillis(connectionTimeoutMs))
                .setReadTimeout(Duration.ofMillis(readTimeoutMs))
                .build();
    }
}
