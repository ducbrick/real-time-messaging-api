package com.ducbrick.real_time_messaging_api.config;

import com.ducbrick.real_time_messaging_api.dtos.UserDetailsDto;
import com.ducbrick.real_time_messaging_api.entities.User;
import com.ducbrick.real_time_messaging_api.repos.UserRepo;
import com.ducbrick.real_time_messaging_api.services.proxies.AuthServerProxy;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.oauth2.client.servlet.OAuth2ClientAutoConfiguration;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
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
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

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
    @GetMapping
    public String get() {
      return "Hello World";
    }

    @PostMapping
    public String post() {
      return "Hello World POST";
    }

    @GetMapping("/principal")
    public UserDetailsDto getPrincipal(@AuthenticationPrincipal UserDetailsDto principal) {
      return principal;
    }
  }

  @Autowired private MockMvc mvc;
  @Autowired private ObjectMapper objectMapper;
  @MockitoBean private JwtDecoder jwtDecoder;
  @MockitoBean private AuthServerProxy authServer;

  @DisplayName("Unauthenticated request should return 401")
  @Test
  public void unauthenticated_401() throws Exception {
    mvc
        .perform(get("/test"))
        .andExpect(status().isUnauthorized());
  }

  @DisplayName("Request with OIDC token should be authenticated")
  @Test
  public void requestWithOIDCToken_authenticated() throws Exception {
    mvc
        .perform(
            get("/test")
            .with(oidcLogin())
        )
        .andExpectAll(
            status().isOk(),
            content().string("Hello World"));
  }

  @DisplayName("POST request shouldnt need CSRF token")
  @Test
  public void postRequestNoCSRF() throws Exception {
    mvc
        .perform(post("/test")
            .with(oidcLogin())
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
    String tokenValue = "random";
    String issuer = "https://ducbrick.us.auth0.com";
    String sub = "41797103410324198";
    String name = "John Doe";
    String email = "jdoe@me.com";

    Jwt jwt = Jwt
        .withTokenValue(tokenValue)
        .header("alg", "RS256")
        .issuer(issuer)
        .subject(sub)
        .build();

    when(jwtDecoder.decode(tokenValue)).thenReturn(jwt);

    Map<String, String> userInfo = new HashMap<>();
    userInfo.put("name", name);
    userInfo.put("email", email);
    when(authServer.getUserInfo(jwt.getTokenValue())).thenReturn(userInfo);

    MvcResult result = mvc
        .perform(
            get("/test/principal")
            .header("Authorization", "Bearer " + tokenValue)
        )
        .andExpect(status().isOk())
        .andReturn();

    UserDetailsDto principal = objectMapper.readValue(result.getResponse().getContentAsString(), UserDetailsDto.class);

    assertThat(principal.id()).isNotNull();
    assertThat(principal.name()).isEqualTo(name);
    assertThat(principal.email()).isEqualTo(email);
  }
}