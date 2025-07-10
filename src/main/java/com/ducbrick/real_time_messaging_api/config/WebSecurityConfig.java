package com.ducbrick.real_time_messaging_api.config;

import com.ducbrick.real_time_messaging_api.services.auth.CustomJwtAuthenticationConverter;
import com.ducbrick.real_time_messaging_api.services.persistence.UserPersistenceService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.authorization.AuthorizationManagers;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.oauth2.core.authorization.OAuth2AuthorizationManagers;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;

import static org.springframework.security.authorization.AuthorizationManagers.allOf;
import static org.springframework.security.oauth2.core.authorization.OAuth2AuthorizationManagers.hasScope;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig {
	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http,
	                                               UserPersistenceService userPersistenceService,
	                                               JwtGrantedAuthoritiesConverter jwtGrantedAuthoritiesConverter) throws Exception {
		http
				.csrf(AbstractHttpConfigurer::disable)
				.oauth2ResourceServer(oa -> oa
					.jwt(jwt -> jwt
						.jwtAuthenticationConverter(new CustomJwtAuthenticationConverter(jwtGrantedAuthoritiesConverter, userPersistenceService)))
				)
				.authorizeHttpRequests(auth -> auth
						.requestMatchers("/public/**").permitAll()
						.requestMatchers("/error/**").permitAll()
						.requestMatchers (
										"/api/v1/auth/**",
										"/v2/api-docs",
										"/v3/api-docs",
										"/v3/api-docs/**",
										"/swagger-resources",
										"/swagger-resources/**",
										"/configuration/ui",
										"/configuration/security",
										"/swagger-ui/**",
										"/webjars/**",
										"/swagger-ui.html"
								).permitAll()
						.anyRequest().access(allOf(hasScope("openid"), hasScope("profile"), hasScope("email")))
				);

		return http.build();
	}

	@Bean
	public JwtGrantedAuthoritiesConverter jwtGrantedAuthoritiesConverter() {
		return new JwtGrantedAuthoritiesConverter();
	}
}
