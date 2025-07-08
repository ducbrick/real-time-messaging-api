package com.ducbrick.real_time_messaging_api.repos;

import com.ducbrick.real_time_messaging_api.entities.Message;
import com.ducbrick.real_time_messaging_api.entities.User;
import com.ducbrick.real_time_messaging_api.testutils.Generator;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Limit;
import org.springframework.data.domain.ScrollPosition;
import org.springframework.data.domain.Window;
import org.springframework.data.support.WindowIterator;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static com.ducbrick.real_time_messaging_api.testutils.Generator.generateRandomEmail;
import static com.ducbrick.real_time_messaging_api.testutils.Generator.generateRandomString;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class MsgRepoTest {
	@Autowired
	private MsgRepo msgRepo;

	@Autowired
	private UserRepo usrRepo;

	@Autowired
	private EntityManager entityManager;

	private User generateNewUser() {
		return User
				.builder()
				.name(generateRandomString(5))
				.email(generateRandomEmail(5))
				.idProviderUrl("https://ducbrick.us.auth0.com/")
				.idProviderId(generateRandomString(10))
				.build();
	}

	@Test
	@DisplayName("Save a new user and message")
	public void saveNewUsrAndMsg() {
		User usr = usrRepo.save(generateNewUser());
		assertThat(usr.getId()).isNotNull();

		Message msg = Message
				.builder()
				.content("hello")
				.sender(usr)
				.build();

		msg = msgRepo.save(msg);

		assertThat(msg.getId()).isNotNull();
		assertThat(msg.getSender().getId()).isEqualTo(usr.getId());
	}

	@Test
	@DisplayName("Save a new message without a sender")
	public void msgWithoutSender() {
		Message msg = Message
				.builder()
				.content("hello")
				.build();

		assertThat(msg.getSender()).isNull();
		assertThatThrownBy(() -> msgRepo.save(msg));
	}

	@Test
	@DisplayName("Save a new message with sender and receivers")
	public void saveMsgWithSenderReceivers() {
		User sender = usrRepo.save(generateNewUser());
		List<User> receivers = Stream
				.generate(() -> usrRepo.save(generateNewUser()))
				.limit(3)
				.toList();

		Message msg = Message
				.builder()
				.content("hello")
				.sender(sender)
				.build();

		msg.setReceivers(new ArrayList<>());
		for (User receiver : receivers) {
			msg.getReceivers().add(receiver);
		}

		msg = msgRepo.save(msg);

		assertThat(msg.getId()).isNotNull();
		assertThat(msg.getSender().getId()).isEqualTo(sender.getId());

		entityManager.flush();

		int msgId = msg.getId();

		for (User receiver : receivers) {
			entityManager.refresh(receiver);
			assertThat(receiver.getReceivedMsgs()).isNotNull();
			assertThat(receiver.getReceivedMsgs()).isNotEmpty();
			assertThat(receiver.getReceivedMsgs()).anyMatch(receivedMsg -> receivedMsg.getId().equals(msgId));
		}
	}

	@Test
	@DisplayName("Scroll through messaging history")
	public void scrollMsgHistory() {
		User sender = usrRepo.save(generateNewUser());
		List<User> receivers = Stream
				.generate(() -> usrRepo.save(generateNewUser()))
				.limit(3)
				.toList();

		int msgCount = 10;

		for (int i = 0; i < msgCount; i++) {
			msgRepo.save(
						Message
								.builder()
								.content(String.valueOf(i))
								.sender(sender)
								.receivers(receivers)
								.build()
				);
		}

		List<Message> history = msgRepo.getMsgHistory(sender.getId(), receivers.getFirst().getId(), Limit.of(2));

		int cursor = 0;
		int expectedMsgContent = msgCount - 1;

		while (history.isEmpty() == false) {
			for (Message msg : history) {
				assertThat(msg.getContent()).isEqualTo(String.valueOf(expectedMsgContent));
				expectedMsgContent--;
				cursor = msg.getId();
			}

			history = msgRepo.getMsgHistory(sender.getId(), receivers.getFirst().getId(), cursor,Limit.of(2));
		}

		assertThat(expectedMsgContent).isEqualTo(-1);
	}
}