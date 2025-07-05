package com.ducbrick.real_time_messaging_api.controllers;

import com.ducbrick.real_time_messaging_api.dtos.UsrInfoDto;
import com.ducbrick.real_time_messaging_api.entities.User;
import com.ducbrick.real_time_messaging_api.repos.UserRepo;
import com.ducbrick.real_time_messaging_api.testutils.Generator;
import com.ducbrick.real_time_messaging_api.testutils.MockUser;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.net.URLEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;

@SpringBootTest
@AutoConfigureMockMvc
class UsrControllerTest {

	@Autowired
	private MockMvc mvc;

	@MockitoBean
	private JwtDecoder jwtDecoder;

	@Autowired
	private UserRepo usrRepo;

	@Autowired
	private ObjectMapper objectMapper;

	@Test
	@DisplayName("Get user info by id")
	@Transactional
	public void getUsrInfoById() throws Exception {
		MockUser mockusr = Generator.generateMockUsr(jwtDecoder, usrRepo);

		MvcResult reqRes = mvc
				.perform(get("/public/user/info")
						.param("id", String.valueOf(mockusr.usr().getId()))
				)
				.andReturn();

		UsrInfoDto resDto = objectMapper.readValue(reqRes.getResponse().getContentAsString(), UsrInfoDto.class);

		User usr = usrRepo.findById(mockusr.usr().getId()).orElse(null);

		assertThat(resDto.id()).isEqualTo(usr.getId());
		assertThat(resDto.name()).isEqualTo(usr.getName());
		assertThat(resDto.email()).isEqualTo(usr.getEmail());
		assertThat(resDto.issuer()).isEqualTo(usr.getIdProviderUrl());
		assertThat(resDto.sub()).isEqualTo(usr.getIdProviderId());
		assertThat(resDto.numOfSentMsgs()).isEqualTo(usrRepo.countSentMsgs(usr.getId()));
		assertThat(resDto.numOfReceivedMsgs()).isEqualTo(usrRepo.countReceivedMsgs(usr.getId()));
	}

	@Test
	@DisplayName("Get user info by issuer and sub")
	@Transactional
	public void getUsrInfoByIssuerAndSub() throws Exception {
		MockUser mockusr = Generator.generateMockUsr(jwtDecoder, usrRepo);

		MvcResult reqRes = mvc
				.perform(get("/public/user/info")
						.param("issuer", mockusr.usr().getIdProviderUrl())
						.param("sub", mockusr.usr().getIdProviderId())
				)
				.andReturn();

		UsrInfoDto resDto = objectMapper.readValue(reqRes.getResponse().getContentAsString(), UsrInfoDto.class);

		User usr = usrRepo.findById(mockusr.usr().getId()).orElse(null);

		assertThat(resDto.id()).isEqualTo(usr.getId());
		assertThat(resDto.name()).isEqualTo(usr.getName());
		assertThat(resDto.email()).isEqualTo(usr.getEmail());
		assertThat(resDto.issuer()).isEqualTo(usr.getIdProviderUrl());
		assertThat(resDto.sub()).isEqualTo(usr.getIdProviderId());
		assertThat(resDto.numOfSentMsgs()).isEqualTo(usrRepo.countSentMsgs(usr.getId()));
		assertThat(resDto.numOfReceivedMsgs()).isEqualTo(usrRepo.countReceivedMsgs(usr.getId()));
	}

	@Test
	@DisplayName("Get user info by authentication")
	@Transactional
	public void getUsrInfoByAuth() throws Exception {
		MockUser mockusr = Generator.generateMockUsr(jwtDecoder, usrRepo);

		MvcResult reqRes = mvc
				.perform(get("/public/user/info")
						.header("Authorization", "Bearer " + mockusr.jwtVal())
				)
				.andReturn();

		UsrInfoDto resDto = objectMapper.readValue(reqRes.getResponse().getContentAsString(), UsrInfoDto.class);

		User usr = usrRepo.findById(mockusr.usr().getId()).orElse(null);

		assertThat(resDto.id()).isEqualTo(usr.getId());
		assertThat(resDto.name()).isEqualTo(usr.getName());
		assertThat(resDto.email()).isEqualTo(usr.getEmail());
		assertThat(resDto.issuer()).isEqualTo(usr.getIdProviderUrl());
		assertThat(resDto.sub()).isEqualTo(usr.getIdProviderId());
		assertThat(resDto.numOfSentMsgs()).isEqualTo(usrRepo.countSentMsgs(usr.getId()));
		assertThat(resDto.numOfReceivedMsgs()).isEqualTo(usrRepo.countReceivedMsgs(usr.getId()));
	}
}