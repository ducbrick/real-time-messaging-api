package com.ducbrick.real_time_messaging_api.services;

import com.ducbrick.real_time_messaging_api.dtos.MsgFromUsr;
import com.ducbrick.real_time_messaging_api.dtos.MsgToUsr;
import com.ducbrick.real_time_messaging_api.repos.UserRepo;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@Service
@Validated
@RequiredArgsConstructor
public class MessagingService {
	private final UserRepo usrRepo;

	@NotNull
	@Valid
	public List<@NotNull @Valid MsgToUsr> saveNewMsg(@NotNull @Valid MsgFromUsr msg) {
		return null;
	}
}
