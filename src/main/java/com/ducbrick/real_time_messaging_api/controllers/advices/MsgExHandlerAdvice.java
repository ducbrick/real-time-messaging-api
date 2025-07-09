package com.ducbrick.real_time_messaging_api.controllers.advices;

import com.ducbrick.real_time_messaging_api.dtos.UserCredentialDto;
import com.ducbrick.real_time_messaging_api.exceptions.IllegalMessageReceiverException;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.security.Principal;

@ControllerAdvice
@RequiredArgsConstructor
@Validated
public class MsgExHandlerAdvice {

	private final SimpMessagingTemplate template;

	@MessageExceptionHandler(IllegalMessageReceiverException.class)
	public void handleIllegalMsgReceiver(IllegalMessageReceiverException ex, @NotNull Principal usr) {
		template.convertAndSendToUser(usr.getName(), "/queue/error", ex.getMessage());
	}
}
