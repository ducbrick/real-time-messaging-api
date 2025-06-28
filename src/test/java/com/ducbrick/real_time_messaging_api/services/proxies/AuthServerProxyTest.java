package com.ducbrick.real_time_messaging_api.services.proxies;

import com.ducbrick.real_time_messaging_api.dtos.AuthServerUsrInfo;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureMockMvc
class AuthServerProxyTest {
	@Autowired private AuthServerProxy authServer;

	@Value("${app.auth-server.access-token}") private String accessToken;

	@DisplayName("Test constraints applied on params")
	@Test
	public void testConstraints() {
		Assertions.assertThatThrownBy(() -> authServer.getUserInfo(null));
	}

	@DisplayName("Query for user info using access token")
	@Test
	public void queryUsrInfoByAccessToken() {
		AuthServerUsrInfo usrInfo = authServer.getUserInfo(accessToken);

		Assertions.assertThat(usrInfo.nickname()).isEqualTo("ducbrick");
		Assertions.assertThat(usrInfo.email()).isEqualTo("ducbrick@gmail.com");
	}
}