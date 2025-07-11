package com.ducbrick.real_time_messaging_api.config;

import com.ducbrick.real_time_messaging_api.dtos.AuthServerUsrInfo;
import com.ducbrick.real_time_messaging_api.dtos.UserCredentialDto;
import com.ducbrick.real_time_messaging_api.repos.UserRepo;
import com.ducbrick.real_time_messaging_api.services.proxies.AuthServerProxy;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.constraints.NotNull;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;

@SpringBootTest
@AutoConfigureMockMvc
class WebSecurityTest {

	@TestConfiguration
	@RestController
	@RequestMapping("/test")
	static class TestController {
		@GetMapping
		public String get() {
			return "Hello World";
		}

		@PostMapping
		public String post() {
			return "Hello World POST";
		}

		@GetMapping("/principal")
		public UserCredentialDto getPrincipal(@NotNull @AuthenticationPrincipal UserCredentialDto principal) {
			return principal;
		}

		@GetMapping("/principal-name")
		public String getAuthentication(@NotNull Principal principal) {
			return principal.getName();
		}
	}

	@Autowired private MockMvc mvc;
	@Autowired private ObjectMapper objectMapper;
	@Autowired private UserRepo usrRepo;
	@MockitoBean private JwtDecoder jwtDecoder;
	@MockitoBean private AuthServerProxy authServer;

	@DisplayName("Unauthenticated request should return 401")
	@Test
	public void unauthenticated_401() throws Exception {
		mvc
			.perform(get("/test"))
			.andExpect(status().isUnauthorized());
	}

	@DisplayName("Request with JWT should be authenticated")
	@Test
	@Transactional
	public void requestWithJwt_authenticated() throws Exception {
		String name = "John Doe";
		String email = "jdoe@me.com";

		AuthServerUsrInfo userInfo = new AuthServerUsrInfo(name, email);
		when(authServer.getUserInfo(anyString())).thenReturn(userInfo);

		mvc
			.perform(
				get("/test")
					.with(jwt()
						.authorities(
							new SimpleGrantedAuthority("SCOPE_openid"),
							new SimpleGrantedAuthority("SCOPE_profile"),
							new SimpleGrantedAuthority("SCOPE_email")
						)
					)
			)
			.andExpectAll(
				status().isOk(),
				content().string("Hello World"));
	}

	@DisplayName("POST request shouldnt need CSRF token")
	@Test
	@Transactional
	public void postRequestNoCSRF() throws Exception {
		String name = "John Doe";
		String email = "jdoe@me.com";

		AuthServerUsrInfo userInfo = new AuthServerUsrInfo(name, email);
		when(authServer.getUserInfo(anyString())).thenReturn(userInfo);

		mvc
			.perform(post("/test")
				.with(jwt()
						.authorities(
							new SimpleGrantedAuthority("SCOPE_openid"),
							new SimpleGrantedAuthority("SCOPE_profile"),
							new SimpleGrantedAuthority("SCOPE_email")
						))
				.with(csrf().useInvalidToken())
			)
			.andExpectAll(
				status().isOk(),
				content().string("Hello World POST"));
	}

	@DisplayName("Authentication principal should be resolved")
	@Test
	@Transactional
	public void resolveAuthenticationPrincipal() throws Exception {
		String tokenVal = "random";
		String issuer = "https://ducbrick.us.auth0.com";
		String sub = "41797103410324198";
		String name = "John Doe";
		String email = "jdoe@me.com";

		Jwt jwt = Jwt
			.withTokenValue(tokenVal)
			.header("alg", "none")
			.issuer(issuer)
			.subject(sub)
			.claim("scope", "openid profile email")
			.build();

		when(jwtDecoder.decode(tokenVal)).thenReturn(jwt);

		AuthServerUsrInfo userInfo = new AuthServerUsrInfo(name, email);
		when(authServer.getUserInfo(tokenVal)).thenReturn(userInfo);

		MvcResult result = mvc
			.perform(
				get("/test/principal")
					.header("Authorization", "Bearer " + tokenVal)
			)
			.andExpect(status().isOk())
			.andReturn();

		UserCredentialDto principal = objectMapper.readValue(result.getResponse().getContentAsString(), UserCredentialDto.class);

		assertThat(principal.id()).isNotNull();
		assertThat(principal.name()).isEqualTo(name);
		assertThat(principal.email()).isEqualTo(email);
	}

	@DisplayName("Authentication object's name is the same as user id")
	@Test
	@Transactional
	public void authenticationNameIsUsrId() throws Exception {
		String tokenVal = "random";
		String issuer = "https://ducbrick.us.auth0.com";
		String sub = "41797103410324198";
		String name = "John Doe";
		String email = "jdoe@me.com";

		Jwt jwt = Jwt
				.withTokenValue(tokenVal)
				.header("alg", "none")
				.issuer(issuer)
				.subject(sub)
				.claim("scope", "openid profile email")
				.build();

		when(jwtDecoder.decode(tokenVal)).thenReturn(jwt);

		AuthServerUsrInfo userInfo = new AuthServerUsrInfo(name, email);
		when(authServer.getUserInfo(tokenVal)).thenReturn(userInfo);

		MvcResult result = mvc
				.perform(
						get("/test/principal-name")
								.header("Authorization", "Bearer " + tokenVal)
				)
				.andExpect(status().isOk())
				.andReturn();

		String usrId = usrRepo.findByIssuerAndSub(issuer, sub).get().getId().toString();

		assertThat(result.getResponse().getContentAsString()).isEqualTo(usrId);
	}
}