package com.ducbrick.real_time_messaging_api.controllers;

import com.ducbrick.real_time_messaging_api.dtos.MsgHistoryDto;
import com.ducbrick.real_time_messaging_api.dtos.MsgToUsr;
import com.ducbrick.real_time_messaging_api.entities.Message;
import com.ducbrick.real_time_messaging_api.entities.User;
import com.ducbrick.real_time_messaging_api.repos.MsgRepo;
import com.ducbrick.real_time_messaging_api.repos.UserRepo;
import com.ducbrick.real_time_messaging_api.testutils.Generator;
import com.ducbrick.real_time_messaging_api.testutils.MockUser;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Limit;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Random;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

@SpringBootTest
@AutoConfigureMockMvc
class MsgHistoryControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private MsgRepo msgRepo;

	@Autowired
	private UserRepo usrRepo;

	@MockitoBean
	private JwtDecoder jwtDecoder;

	@Test
	@DisplayName("Scroll through messaging history")
	@Transactional
	public void scrollMsgHistory() throws Exception {
		MockUser mockSender = Generator.generateMockUsr(jwtDecoder, usrRepo);
		MockUser mockReceiver = Generator.generateMockUsr(jwtDecoder, usrRepo);

		int msgCount = 10;

		for (int i = 0; i < msgCount; i++) {
			User usr1 = new Random().nextBoolean() ? mockSender.usr() : mockReceiver.usr();
			User usr2 = usr1 == mockSender.usr() ? mockReceiver.usr() : mockSender.usr();

			System.out.println(usr1.getId() + " -> " + usr2.getId());

			msgRepo.save(
					Message
							.builder()
							.content(String.valueOf(i))
							.sender(usr1)
							.receivers(List.of(usr2))
							.build()
			);
		}

		int expectedMsgContent = msgCount - 1;
		Integer cursor = null;

		while (true) {
			MockHttpServletRequestBuilder reqBuilder = get("/history")
					.header("Authorization", "Bearer " + mockSender.jwtVal())
					.param("receiverId", String.valueOf(mockReceiver.usr().getId()));

			if (cursor != null) {
				reqBuilder = reqBuilder.param("cursor", String.valueOf(cursor));
			}

			MvcResult reqRes = mockMvc
					.perform(reqBuilder)
					.andReturn();

			MsgHistoryDto resDto = objectMapper.readValue(reqRes.getResponse().getContentAsString(), MsgHistoryDto.class);

			if (resDto.msgs() == null || resDto.msgs().isEmpty()) {
				break;
			}

			for (MsgToUsr msg : resDto.msgs()) {
				assertThat(msg.content()).isEqualTo(String.valueOf(expectedMsgContent));
				expectedMsgContent--;
			}

			cursor = resDto.cursor();
		}

		assertThat(expectedMsgContent).isEqualTo(-1);
	}
}