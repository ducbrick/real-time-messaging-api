package com.ducbrick.real_time_messaging_api.config;

import com.ducbrick.real_time_messaging_api.dtos.AuthServerUsrInfo;
import com.ducbrick.real_time_messaging_api.dtos.MsgFromUsr;
import com.ducbrick.real_time_messaging_api.entities.User;
import com.ducbrick.real_time_messaging_api.repos.UserRepo;
import com.ducbrick.real_time_messaging_api.services.proxies.AuthServerProxy;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.messaging.converter.CompositeMessageConverter;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.converter.StringMessageConverter;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.annotation.SubscribeMapping;
import org.springframework.messaging.simp.stomp.*;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.stereotype.Controller;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.*;

import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class WebSocketSecurityConfigTest {
	@TestConfiguration
	@Controller
	public static class TestController {
		@MessageMapping("/send/secret")
		public void send(String msg) {
		}

		@SubscribeMapping("/sub/secret")
		public String sub() {
			return "Hello";
		}
	}

	@LocalServerPort
	private Integer port;

	@MockitoBean
	private JwtDecoder jwtDecoder;
	@MockitoBean private AuthServerProxy authServer;

	@Autowired
	private UserRepo usrRepo;

	private final String jwtVal = "sdkfljasdlf";
	private final String issuer = "https://ducbrick.us.auth0.com";
	private final String sub = "41797103410324198";
	private final String name = "John Doe";
	private final String email = "jdoe@me.com";

	private final Logger logger = org.slf4j.LoggerFactory.getLogger(WebSocketSecurityConfigTest.class);
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
	@DisplayName("Attempt an unauthorized subscription")
	public void unauthorizedSub() throws ExecutionException, InterruptedException, TimeoutException {
		CountDownLatch errCnt = new CountDownLatch(1);

		wsClient.setMessageConverter(new StringMessageConverter());

		wsClient.connectAsync(getWsUri(), getWsHandshakeHeaders(), new StompSessionHandlerAdapter() {
			@Override
			public void handleFrame(StompHeaders headers, Object payload) {
				errCnt.countDown();
			}

			@Override
			public void handleException(StompSession session, StompCommand command, StompHeaders headers, byte[] payload, Throwable exception) {
				logger.debug("Handling exception with command: {} and headers: {}", command, headers);
			}

			@Override
			public void afterConnected(StompSession session, StompHeaders connectedHeaders) {
				session.subscribe("/topic/secret", new StompFrameHandler() {
					@Override
					public Type getPayloadType(StompHeaders headers) {
						return String.class;
					}

					@Override
					public void handleFrame(StompHeaders headers, Object payload) {
						throw new RuntimeException();
					}
				});
			}
		});

		Assertions.assertThat(errCnt.await(1, TimeUnit.SECONDS)).isTrue();
	}

	@Test
	@DisplayName("Attempt to send a message to an unauthorized destination")
	public void unauthorizedMsg() throws ExecutionException, InterruptedException, TimeoutException {
		CountDownLatch errCnt = new CountDownLatch(1);

		wsClient.setMessageConverter(new StringMessageConverter());

		wsClient.connectAsync(getWsUri(), getWsHandshakeHeaders(), new StompSessionHandlerAdapter() {
			@Override
			public void handleFrame(StompHeaders headers, Object payload) {
				errCnt.countDown();
			}

			@Override
			public void handleException(StompSession session, StompCommand command, StompHeaders headers, byte[] payload, Throwable exception) {
				logger.debug("Handling exception with command: {} and headers: {}", command, headers);
			}

			@Override
			public void afterConnected(StompSession session, StompHeaders connectedHeaders) {
				session.send("/app/secret", "Hello");
			}
		});

		Assertions.assertThat(errCnt.await(1, TimeUnit.SECONDS)).isTrue();
	}
}