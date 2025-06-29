package com.ducbrick.real_time_messaging_api.services;

import com.ducbrick.real_time_messaging_api.dtos.MsgFromUsr;
import com.ducbrick.real_time_messaging_api.dtos.MsgToUsr;
import com.ducbrick.real_time_messaging_api.exceptions.NoSuchUserException;
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

	private void verifyReceiverExistence(@NotNull @Valid MsgFromUsr msg) throws NoSuchUserException {
		List<Integer> receiversIds = msg.receiversIds();

		for (int receiverId : receiversIds) {
			if (usrRepo.existsById(receiverId) == false) {
				throw new NoSuchUserException("User with id " + receiverId + " doesn't exist");
			}
		}
	}

	@NotNull
	@Valid
	public List<@NotNull @Valid MsgToUsr> saveNewMsg(@NotNull @Valid MsgFromUsr msg) throws NoSuchUserException {
		verifyReceiverExistence(msg);
		return null;
	}
}
