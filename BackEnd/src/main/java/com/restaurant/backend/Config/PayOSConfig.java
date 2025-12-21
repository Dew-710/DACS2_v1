package com.restaurant.backend.Config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class PayOSConfig {

    private final PayOSProperties payOSProperties;

    public PayOSConfig(PayOSProperties payOSProperties) {
        this.payOSProperties = payOSProperties;
    }

    @Bean
    public WebClient payOSWebClient() {
        return WebClient.builder()
                .baseUrl(payOSProperties.getBaseUrl() != null ? payOSProperties.getBaseUrl() : "")
                .defaultHeader("X-Client-Id", payOSProperties.getClientId() != null ? payOSProperties.getClientId() : "")
                .defaultHeader("Authorization", payOSProperties.getApiKey() != null ? "Bearer " + payOSProperties.getApiKey() : "")
                .build();
    }
}

