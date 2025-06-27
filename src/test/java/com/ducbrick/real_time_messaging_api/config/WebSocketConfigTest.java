package com.ducbrick.real_time_messaging_api.config;

import com.ducbrick.real_time_messaging_api.dtos.AuthServerUsrInfo;
import com.ducbrick.real_time_messaging_api.entities.User;
import com.ducbrick.real_time_messaging_api.repos.UserRepo;
import com.ducbrick.real_time_messaging_api.services.proxies.AuthServerProxy;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class WebSocketConfigTest {
	@LocalServerPort private Integer port;

	@MockitoBean private JwtDecoder jwtDecoder;
	@MockitoBean private AuthServerProxy authServer;

	@Autowired private UserRepo usrRepo;

	private final String jwtVal = "sdkfljasdlf";
	private final String issuer = "https://ducbrick.us.auth0.com";
	private final String sub = "41797103410324198";
	private final String name = "John Doe";
	private final String email = "jdoe@me.com";

	private WebSocketStompClient wsClient;

	@BeforeEach
	public void setup() {
		wsClient =
				new WebSocketStompClient(
						new SockJsClient(
								List.of(new WebSocketTransport(
										new StandardWebSocketClient()
								))
						)
				);

		Jwt jwt = Jwt
				.withTokenValue(jwtVal)
				.header("alg", "none")
				.issuer(issuer)
				.subject(sub)
				.claim("scope", "openid profile email")
				.build();

		when(jwtDecoder.decode(jwtVal)).thenReturn(jwt);

		AuthServerUsrInfo userInfo = new AuthServerUsrInfo(name, email);
		when(authServer.getUserInfo(jwtVal)).thenReturn(userInfo);
	}

	@AfterEach
	public void cleanUp() {
		Optional<User> usr = usrRepo.findByIssuerAndSub(issuer, sub);
		usr.ifPresent(user -> usrRepo.deleteById(user.getId()));
	}

	private String getWsUri() {
		return String.format("ws://localhost:%d/msg", port);
	}

	private WebSocketHttpHeaders getWsHandshakeHeaders() {
		WebSocketHttpHeaders headers = new WebSocketHttpHeaders();
		headers.add("Authorization", String.format("Bearer %s", jwtVal));
		return headers;
	}

	@Test
	@DisplayName("Open a STOMP Websocket connection")
	public void openWsConnection() throws ExecutionException, InterruptedException, TimeoutException {
		StompSession session = wsClient
				.connectAsync(getWsUri(), getWsHandshakeHeaders(), new StompSessionHandlerAdapter() {})
				.get(1, TimeUnit.SECONDS);
	}
}