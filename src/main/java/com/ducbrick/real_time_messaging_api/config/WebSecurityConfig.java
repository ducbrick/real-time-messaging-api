package com.ducbrick.real_time_messaging_api.config;

import com.ducbrick.real_time_messaging_api.services.auth.CustomJwtAuthenticationConverter;
import com.ducbrick.real_time_messaging_api.services.persistence.UserPersistenceService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig {
	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http, UserPersistenceService userPersistenceService) throws Exception {
		http
				.csrf(AbstractHttpConfigurer::disable)
				.oauth2ResourceServer(oa -> oa
					.jwt(Customizer.withDefaults())
				)
				.authorizeHttpRequests(auth -> auth
					.anyRequest().authenticated()
				);

		return http.build();
	}
}
