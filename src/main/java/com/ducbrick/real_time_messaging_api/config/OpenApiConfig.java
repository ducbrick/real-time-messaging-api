package com.ducbrick.real_time_messaging_api.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.*;
import io.swagger.v3.oas.annotations.servers.Server;

@OpenAPIDefinition(
		info = @Info(
				title = "API Documentation",
				description = "API documentation for a simple real-time messaging application",
				version = "1.0.0",
				contact = @Contact(
						name = "ducbrick",
						email = "ducbrick@gmail.com"
				)
		),
		servers = {
				@Server(
						description = "Local",
						url = "http://localhost:8080"
				)
		},
		security = {
				@SecurityRequirement(
						name = "OAuth2"
				)
		}
)
@SecurityScheme(
		type = SecuritySchemeType.HTTP,
		name = "OAuth2",
		description = "OAuth2 bearer JWT",
		scheme = "bearer",
		bearerFormat = "JWT"
)
public class OpenApiConfig {
}
