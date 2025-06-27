package com.ducbrick.real_time_messaging_api.controllers.test;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SubscribeMapping;
import org.springframework.stereotype.Controller;

@Controller
@Profile("test")
@RequiredArgsConstructor
public class DemoMsgController {
	private final SimpMessagingTemplate messagingTemplate;

	@MessageMapping("/msg")
	public String handleMsg(String msg) {
		return msg;
	}

	@SubscribeMapping("/sub")
	public String handleSub() {
		return "welcome";
	}
}
