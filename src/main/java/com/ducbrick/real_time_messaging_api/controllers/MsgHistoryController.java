package com.ducbrick.real_time_messaging_api.controllers;

import com.ducbrick.real_time_messaging_api.dtos.MsgHistoryDto;
import com.ducbrick.real_time_messaging_api.services.MsgHistoryService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Validated
@Tag(name = "Message history")
public class MsgHistoryController {

	private final MsgHistoryService msgHistoryService;

	@Valid
	@NotNull
	@GetMapping("/history")
	public MsgHistoryDto getMsgHistory(@RequestParam(required = false) Integer cursor, @RequestParam int receiverId) {
		return msgHistoryService.getMsgHistory(cursor, receiverId);
	}
}
