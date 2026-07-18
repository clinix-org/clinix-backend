package com.gestao.clinix.config;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
public class CorsConfig {
	private final List<String> allowedOrigins;

	public CorsConfig(@Value("${app.cors.allowed-origins}") String allowedOrigins) {
		this.allowedOrigins = Arrays.stream(allowedOrigins.split(","))
				.map(String::trim)
				.filter(origin -> !origin.isEmpty())
				.toList();

		if (this.allowedOrigins.isEmpty() || this.allowedOrigins.contains("*")) {
			throw new IllegalArgumentException(
					"CORS_ALLOWED_ORIGINS deve conter origens explicitas; '*' nao e permitido com credenciais.");
		}
	}

	@Bean
	public CorsConfigurationSource corsConfigurationSource() {
		CorsConfiguration config = new CorsConfiguration();

		config.setAllowedOriginPatterns(allowedOrigins);
		config.setAllowedHeaders(List.of("Authorization", "Content-Type", "X-Request-ID"));
		config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
		config.setAllowCredentials(true);

		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", config);

		return source;
	}
}
