package com.ducbrick.real_time_messaging_api.controllers;

import com.ducbrick.real_time_messaging_api.dtos.MsgFromUsr;
import com.ducbrick.real_time_messaging_api.dtos.MsgToUsr;
import com.ducbrick.real_time_messaging_api.services.MessagingService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@Controller
@Validated
@RequiredArgsConstructor
public class MsgController {
	private final SimpMessagingTemplate messagingTemplate;
	private final MessagingService messagingService;

	@MessageMapping("/private-msg")
	public void sendPrivateMsg(@NotNull @Valid MsgFromUsr msg) {
		List<MsgToUsr> outGoingMsgs = messagingService.saveNewMsg(msg);

		for (MsgToUsr outGoingMsg : outGoingMsgs) {
			String receiverId = String.valueOf(outGoingMsg.receiverId());
			messagingTemplate.convertAndSendToUser(receiverId, "/queue/private-msg", outGoingMsg);
		}
	}
}
