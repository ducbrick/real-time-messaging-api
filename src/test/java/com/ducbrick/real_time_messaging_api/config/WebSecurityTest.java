package com.ducbrick.real_time_messaging_api.config;

import com.ducbrick.real_time_messaging_api.entities.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.oauth2.client.servlet.OAuth2ClientAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.test.web.servlet.response.SecurityMockMvcResultHandlers;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultHandlers.exportTestSecurityContext;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;

@SpringBootTest
@AutoConfigureMockMvc
class WebSecurityTest {
  @TestConfiguration
  @RestController
  @RequestMapping("/test")
  static class TestController {
    @GetMapping("/principal")
    public User getPrincipal(@AuthenticationPrincipal User principal) {
      return principal;
    }
  }

  @Autowired private MockMvc mvc;
  @Autowired private ObjectMapper objectMapper;
  @MockitoBean private JwtDecoder jwtDecoder;

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

  @DisplayName("Authentication principal should be resolved")
  @Test
  public void resolveAuthenticationPrincipal() throws Exception {
    Jwt jwt = Jwt
        .withTokenValue("random")
        .header("alg", "RS256")
        .issuer("https://accounts.google.com")
        .subject("41797103410324198")
        .claim("name", "John Doe")
        .claim("email", "jdoe@me.com")
        .build();

    when(jwtDecoder.decode(Mockito.anyString())).thenReturn(jwt);

    MvcResult result = mvc
        .perform(
            get("/test/principal")
            .header("Authorization", "Bearer random")
        )
        .andExpect(status().isOk())
        .andReturn();

    User principal = objectMapper.readValue(result.getResponse().getContentAsString(), User.class);

    assertThat(principal.getName()).isEqualTo("John Doe");
    assertThat(principal.getEmail()).isEqualTo("jdoe@me.com");
    assertThat(principal.getIdProviderUrl()).isEqualTo("https://accounts.google.com");
    assertThat(principal.getIdProviderId()).isEqualTo("41797103410324198");
  }
}