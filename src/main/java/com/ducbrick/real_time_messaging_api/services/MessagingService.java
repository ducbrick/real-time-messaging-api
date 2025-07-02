package com.ducbrick.real_time_messaging_api.services;

import com.ducbrick.real_time_messaging_api.dtos.MsgFromUsr;
import com.ducbrick.real_time_messaging_api.dtos.MsgToUsr;
import com.ducbrick.real_time_messaging_api.exceptions.IllegalMessageReceiverException;
import com.ducbrick.real_time_messaging_api.repos.UserRepo;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.ArrayList;
import java.util.List;

@Service
@Validated
@RequiredArgsConstructor
public class MessagingService {
	private final UserRepo usrRepo;

	private void verifyReceiverExistence(@NotNull @Valid MsgFromUsr msg) throws IllegalMessageReceiverException {
		List<Integer> receiversIds = msg.receiversIds();

		for (int receiverId : receiversIds) {
			if (usrRepo.existsById(receiverId) == false) {
				throw new IllegalMessageReceiverException("User with id " + receiverId + " doesn't exist");
			}
		}
	}
	private void ensureNoSelfMessaging(@NotNull @Valid MsgFromUsr msg) throws IllegalMessageReceiverException {
		List<Integer> receiversIds = msg.receiversIds();

		String senderId = SecurityContextHolder.getContext().getAuthentication().getName();

		for (int receiverId : receiversIds) {
			if (String.valueOf(receiverId).equals(senderId)) {
				throw new IllegalMessageReceiverException("Sending a message to oneself is not supported");
			}
		}
	}

	@NotNull
	@Valid
	public List<@NotNull @Valid MsgToUsr> saveNewMsg(@NotNull @Valid MsgFromUsr msg) throws IllegalMessageReceiverException {
		verifyReceiverExistence(msg);
		ensureNoSelfMessaging(msg);

		int senderId = Integer.parseInt(SecurityContextHolder.getContext().getAuthentication().getName());

		List<MsgToUsr> outGoingMsgs = msg
				.receiversIds()
				.stream()
				.map(receiverId -> MsgToUsr
						.builder()
						.content(msg.content())
						.senderId(senderId)
						.receiverId(receiverId)
						.build())
				.toList();

		return outGoingMsgs;
	}
}
