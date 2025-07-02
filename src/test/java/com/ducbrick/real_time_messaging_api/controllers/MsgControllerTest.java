package com.ducbrick.real_time_messaging_api.controllers;

import com.ducbrick.real_time_messaging_api.dtos.MsgFromUsr;
import com.ducbrick.real_time_messaging_api.dtos.MsgToUsr;
import com.ducbrick.real_time_messaging_api.entities.User;
import com.ducbrick.real_time_messaging_api.repos.UserRepo;
import org.assertj.core.api.Assertions;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class MsgControllerTest {
	private record MockUser(
			String jwtVal,
			User usr
	) {}

	@LocalServerPort
	private Integer port;

	@Autowired
	private UserRepo usrRepo;

	@MockitoBean
	private JwtDecoder jwtDecoder;

	private List<MockUser> mockUsers;
	private WebSocketStompClient wsClient;

	@BeforeEach
	public void setup() {
		mockUsers = new ArrayList<>();

		wsClient =
				new WebSocketStompClient(
						new SockJsClient(
								List.of(new WebSocketTransport(
										new StandardWebSocketClient()
								))
						)
				);

		ThreadPoolTaskScheduler taskScheduler = new ThreadPoolTaskScheduler();
		taskScheduler.afterPropertiesSet();

		wsClient.setTaskScheduler(taskScheduler);
	}

	@AfterEach
	public void cleanUp() {
		for (MockUser mockUser : mockUsers) {
			usrRepo.deleteById(mockUser.usr().getId());
		}
	}

	private String generateRandomString(int length) {
		return new Random()
				.ints('a', 'z' + 1)
				.limit(length)
				.collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
				.toString();
	}
	private MockUser generateMockUsr() {
		String jwtVal = generateRandomString(10);

		User usr = usrRepo.save(User
				.builder()
				.name(generateRandomString(5))
				.email(generateRandomString(5) + "@gmail.com")
				.idProviderId(generateRandomString(10))
				.idProviderUrl("https://ducbrick.us.auth0.com")
				.build()
		);

		Mockito
				.when(jwtDecoder.decode(jwtVal))
				.thenReturn(Jwt
						.withTokenValue(jwtVal)
						.header("alg", "none")
						.issuer(usr.getIdProviderUrl())
						.subject(usr.getIdProviderId())
						.claim("scope", "openid profile email")
						.build());

		MockUser mockUser = new MockUser(jwtVal, usr);
		mockUsers.add(mockUser);
		return mockUser;
	}

	private String getWsUri() {
		return String.format("ws://localhost:%d/msg", port);
	}

	private WebSocketHttpHeaders getWsHandshakeHeaders(MockUser mockUsr) {
		WebSocketHttpHeaders headers = new WebSocketHttpHeaders();
		headers.add("Authorization", String.format("Bearer %s", mockUsr.jwtVal));
		return headers;
	}

	@Test
	@DisplayName("One sender sends a message to multiple receivers")
	public void oneSender_multipleReceivers() throws ExecutionException, InterruptedException, TimeoutException {
		MockUser sender = generateMockUsr();
		List<MockUser> receivers = IntStream
				.range(0, 3)
				.mapToObj(index -> generateMockUsr())
				.toList();

		String msgContent = "xyz";

		wsClient.setMessageConverter(new MappingJackson2MessageConverter());

		StompSession senderSession = wsClient
				.connectAsync(getWsUri(), getWsHandshakeHeaders(sender), new StompSessionHandlerAdapter() {})
				.get(1, TimeUnit.SECONDS);

		List<CompletableFuture<StompSession>> receiverSessions = new ArrayList<>();
			List<CompletableFuture<MsgToUsr>> receivedMsgs = new ArrayList<>();

		for (MockUser receiver : receivers) {
			CompletableFuture<MsgToUsr> receivedMsg = new CompletableFuture<>();
			receivedMsgs.add(receivedMsg);

			receiverSessions.add(wsClient.connectAsync(getWsUri(), getWsHandshakeHeaders(receiver), new StompSessionHandlerAdapter() {
				@Override
				public void afterConnected(StompSession session, StompHeaders connectedHeaders) {

					session.subscribe("/user/queue/private-msg", new StompFrameHandler() {
						@Override
						public Type getPayloadType(StompHeaders headers) {
							return MsgToUsr.class;
						}

						@Override
						public void handleFrame(StompHeaders headers, Object payload) {
							receivedMsg.complete((MsgToUsr) payload);
						}
					});

				}
			}));
		}

		for (CompletableFuture<StompSession> receiverSession : receiverSessions) {
			receiverSession.get();
		}

		MsgFromUsr msg = new MsgFromUsr(
				msgContent,
				receivers.stream().map(receiver -> receiver.usr().getId()).toList()
		);

		senderSession.setAutoReceipt(true);
		assertThat(senderSession.send("/app/private-msg", msg)).isNotNull();

		for (int i = 0; i < receivers.size(); i++) {
			MockUser receiver = receivers.get(i);
			CompletableFuture<MsgToUsr> receivedMsgFuture = receivedMsgs.get(i);

			await().atMost(1, TimeUnit.SECONDS).untilAsserted(() -> {
				assertThat(receivedMsgFuture.isDone()).isTrue();

				MsgToUsr receivedMsg = receivedMsgFuture.resultNow();

				assertThat(receivedMsg.content()).isEqualTo(msgContent);
				assertThat(receivedMsg.senderId()).isEqualTo(sender.usr().getId());
				assertThat(receivedMsg.receiverId()).isEqualTo(receiver.usr().getId());
			});
		}
	}

	@Test
	@DisplayName("User attempts to send a message to themselves")
	public void msgToOneSelf() throws ExecutionException, InterruptedException, TimeoutException {
		MockUser usr = generateMockUsr();

		wsClient.setMessageConverter(new MappingJackson2MessageConverter());

		StompSession session = wsClient
				.connectAsync(getWsUri(), getWsHandshakeHeaders(usr), new StompSessionHandlerAdapter() {})
				.get(1, TimeUnit.SECONDS);

		session.subscribe("/user/queue/private-msg", new StompFrameHandler() {
			@Override
			public Type getPayloadType(StompHeaders headers) {
				return MsgToUsr.class;
			}

			@Override
			public void handleFrame(StompHeaders headers, Object payload) {
				throw new RuntimeException();
			}
		});

		MsgFromUsr payload = new MsgFromUsr("xyz", List.of(usr.usr().getId()));

		session.setAutoReceipt(true);
		StompSession.Receiptable receipt = session.send("/app/private-msg", payload);

		assertThat(receipt.getReceiptId()).isNotNull();
	}
}