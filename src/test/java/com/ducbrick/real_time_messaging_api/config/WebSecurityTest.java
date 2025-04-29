package com.ducbrick.real_time_messaging_api.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.oauth2.client.servlet.OAuth2ClientAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;

@SpringBootTest
@AutoConfigureMockMvc
class WebSecurityTest {
	@Autowired private MockMvc mvc;

	@DisplayName("Unauthenticated request should return 401")
	@Test
	public void unauthenticated_401() throws Exception {
		mvc
				.perform(get("/"))
				.andExpect(status().isUnauthorized());
	}

	@DisplayName("Request with OIDC token should be authenticated")
	@Test
	public void requestWithOIDCToken_authenticated() throws Exception {
		mvc
				.perform(get("/")
						.with(oidcLogin()))
				.andExpectAll(
						status().isOk(),
						content().string("Hello World"));
	}

	@DisplayName("POST request shouldnt need CSRF token")
	@Test
	public void postRequestNoCSRF() throws Exception {
		mvc
				.perform(post("/")
						.with(oidcLogin()))
				.andExpectAll(
						status().isOk(),
						content().string("Hello World POST"));
	}
}