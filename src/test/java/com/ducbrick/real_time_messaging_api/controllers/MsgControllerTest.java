package com.ducbrick.real_time_messaging_api.controllers;

import com.ducbrick.real_time_messaging_api.dtos.MsgFromUsr;
import com.ducbrick.real_time_messaging_api.dtos.MsgToUsr;
import com.ducbrick.real_time_messaging_api.entities.Message;
import com.ducbrick.real_time_messaging_api.entities.User;
import com.ducbrick.real_time_messaging_api.repos.MsgRepo;
import com.ducbrick.real_time_messaging_api.repos.UserRepo;
import com.ducbrick.real_time_messaging_api.testutils.Generator;
import com.ducbrick.real_time_messaging_api.testutils.MockUser;
import jakarta.persistence.EntityManager;
import org.assertj.core.api.Assertions;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.messaging.converter.CompositeMessageConverter;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.converter.StringMessageConverter;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static com.ducbrick.real_time_messaging_api.testutils.Generator.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.mockito.Mockito.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class MsgControllerTest {

	@LocalServerPort
	private Integer port;

	@Autowired
	private TransactionTemplate transactionTemplate;

	@Autowired
	private UserRepo usrRepo;

	@MockitoSpyBean
	private MsgRepo msgRepo;

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
		transactionTemplate.execute(new TransactionCallbackWithoutResult() {
			@Override
			protected void doInTransactionWithoutResult(TransactionStatus status) {
				for (MockUser mockUsr : mockUsers) {
					User usr = usrRepo.findById(mockUsr.usr().getId()).orElse(null);

					if (usr == null) {
						continue;
					}

					for (Message msg : usr.getReceivedMsgs()) {
						msg.setSender(null);
						msg.getReceivers().clear();
						msgRepo.delete(msg);
					}
				}

				for (MockUser mockUsr : mockUsers) {
					usrRepo.deleteById(mockUsr.usr().getId());
				}
			}
		});
	}

	private String getWsUri() {
		return String.format("ws://localhost:%d/msg", port);
	}

	private WebSocketHttpHeaders getWsHandshakeHeaders(MockUser mockUsr) {
		WebSocketHttpHeaders headers = new WebSocketHttpHeaders();
		headers.add("Authorization", String.format("Bearer %s", mockUsr.jwtVal()));
		return headers;
	}

	@Test
	@DisplayName("One sender sends a message to multiple receivers")
	public void oneSender_multipleReceivers() throws ExecutionException, InterruptedException, TimeoutException {
		MockUser sender = generateMockUsr(jwtDecoder, usrRepo, mockUsers);
		List<MockUser> receivers = Stream
				.generate(() -> generateMockUsr(jwtDecoder, usrRepo, mockUsers))
				.limit(3)
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

		ArgumentCaptor<Message> msgArgCaptor = ArgumentCaptor.forClass(Message.class);

		verify(msgRepo, times((1))).save(msgArgCaptor.capture());

		Message savedMsg = msgArgCaptor.getValue();

		assertThat(savedMsg.getContent()).isEqualTo(msgContent);
		assertThat(savedMsg.getSender().getId()).isEqualTo(sender.usr().getId());
		for (MockUser receiver : receivers) {
			assertThat(savedMsg.getReceivers()).anyMatch(r -> r.getId().equals(receiver.usr().getId()));
		}
	}

	@Test
	@DisplayName("User attempts to send a message to themselves")
	public void msgToOneSelf() throws ExecutionException, InterruptedException, TimeoutException {
		MockUser usr = generateMockUsr(jwtDecoder, usrRepo, mockUsers);

		wsClient.setMessageConverter(new CompositeMessageConverter(
				List.of(
						new MappingJackson2MessageConverter(),
						new StringMessageConverter())
				));

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

		CountDownLatch errCnt = new CountDownLatch(1);

		session.subscribe("/user/queue/error", new StompFrameHandler() {
			@Override
			public Type getPayloadType(StompHeaders headers) {
				return String.class;
			}

			@Override
			public void handleFrame(StompHeaders headers, Object payload) {
				errCnt.countDown();
			}
		});

		MsgFromUsr payload = new MsgFromUsr("xyz", List.of(usr.usr().getId()));

		session.setAutoReceipt(true);
		StompSession.Receiptable receipt = session.send("/app/private-msg", payload);

		assertThat(receipt.getReceiptId()).isNotNull();
		verify(msgRepo, times(0)).save(any());
		assertThat(errCnt.await(1, TimeUnit.SECONDS)).isTrue();
	}
}