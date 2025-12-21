package com.restaurant.backend.Config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        // ⚠️ DEVELOPMENT: Allow all origins for ngrok/cloudflare tunnels
        // 🔒 PRODUCTION: Thay bằng domain cụ thể!
        configuration.setAllowedOriginPatterns(Arrays.asList("*"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        configuration.setExposedHeaders(Arrays.asList("Authorization"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                .csrf(csrf -> csrf.disable())
                // Enable CORS for HTTP requests
                
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // IMPORTANT: WebSocket requests must bypass security
                // Cho phép tất cả requests (đơn giản hóa cho demo)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/ws/**").permitAll() // WebSocket - must be first
                        .requestMatchers("/api/**").permitAll() // API endpoints
                        .anyRequest().permitAll() // Tất cả requests khác
                )

                // Tắt login form & http basic
                .httpBasic(c -> c.disable())
                .formLogin(l -> l.disable())
                
                // Disable security headers that might interfere with WebSocket
                .headers(headers -> headers
                        .contentTypeOptions().disable()
                        .frameOptions().disable()
                        .xssProtection(xss -> xss.disable())
                )
                
                // Disable session management for WebSocket
                .sessionManagement(session -> session
                        .sessionCreationPolicy(org.springframework.security.config.http.SessionCreationPolicy.STATELESS)
                );

        return http.build();
    }
}
