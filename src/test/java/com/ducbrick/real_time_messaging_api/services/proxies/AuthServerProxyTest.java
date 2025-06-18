package com.ducbrick.real_time_messaging_api.services.proxies;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureMockMvc
class AuthServerProxyTest {
	@Autowired private AuthServerProxy authServer;

	@DisplayName("Test constraints applied on params")
	@Test
	public void testConstraints() {
		Assertions.assertThatThrownBy(() -> authServer.getUserInfo(null));
	}
}