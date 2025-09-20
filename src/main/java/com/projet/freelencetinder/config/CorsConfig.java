package com.projet.freelencetinder.config;

import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
public class CorsConfig {

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        // Origines locales autorisées (patterns OK avec allowCredentials=true)
        config.setAllowedOriginPatterns(List.of(
            "http://localhost:*",
            "http://127.0.0.1:*"
            // Ajoute ici d'autres frontends si besoin (http://192.168.*:*, etc.)
        ));

        // Méthodes permises
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));

        // ⚠️ Autorise tous les headers (évite d'oublier un header custom : X-Freelancer-Id, X-Client-Id, etc.)
        config.addAllowedHeader("*");

        // Cookies / Authorization
        config.setAllowCredentials(true);

        // Headers exposés côté navigateur (utile pour Set-Cookie, Location, etc.)
        config.setExposedHeaders(List.of(HttpHeaders.SET_COOKIE, HttpHeaders.LOCATION));

        // Cache du preflight (en secondes)
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
