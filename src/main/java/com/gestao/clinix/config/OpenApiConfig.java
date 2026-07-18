package com.gestao.clinix.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;

@Configuration
public class OpenApiConfig {

	private static final String BEARER_AUTH = "bearerAuth";

	@Bean
	OpenAPI clinixOpenAPI() {
		return new OpenAPI()
				.info(new Info()
						.title("Clinix API")
						.description("API para gestao hospitalar")
						.version("v1"))
				.addSecurityItem(new SecurityRequirement().addList(BEARER_AUTH))
				.components(new Components().addSecuritySchemes(BEARER_AUTH,
						new SecurityScheme()
								.name(BEARER_AUTH)
								.type(SecurityScheme.Type.HTTP)
								.scheme("bearer")
								.bearerFormat("JWT")));
	}
}
