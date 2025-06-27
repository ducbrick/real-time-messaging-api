package com.ducbrick.real_time_messaging_api.controllers;

import com.ducbrick.real_time_messaging_api.dtos.MsgFromUsr;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class MsgController {
	private final SimpMessagingTemplate messagingTemplate;

	@MessageMapping("/private-msg")
	public void sendPrivateMsg(MsgFromUsr msg) {
		messagingTemplate.convertAndSendToUser(msg.receiver(), "/queue/private-msg", msg.content());
	}
}
