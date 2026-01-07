package com.restaurant.backend.Config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FrontendConfig {

    @Value("${frontend.url:http://localhost:3000}")
    private String frontendUrl;

    public String getFrontendUrl() {
        return frontendUrl;
    }
}

