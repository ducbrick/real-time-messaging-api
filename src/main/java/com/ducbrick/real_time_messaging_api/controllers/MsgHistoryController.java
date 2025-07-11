package com.ducbrick.real_time_messaging_api.controllers;

import com.ducbrick.real_time_messaging_api.dtos.MsgHistoryDto;
import com.ducbrick.real_time_messaging_api.services.MsgHistoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
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

	@Operation(
			summary = "Get the authenticated user's messaging history with another user"
	)
	@Parameter(
			name = "cursor",
			in = ParameterIn.QUERY,
			description = "Specify the message (exclusive) to start retrieving from",
			schema = @Schema(type = "integer")
	)
	@Parameter(
			name = "receiverId",
			in = ParameterIn.QUERY,
			required = true,
			description = "ID of the user to get the history with",
			schema = @Schema(type = "integer")
	)
	@Valid
	@NotNull
	@GetMapping("/history")
	public MsgHistoryDto getMsgHistory(@RequestParam(required = false) Integer cursor, @RequestParam int receiverId) {
		return msgHistoryService.getMsgHistory(cursor, receiverId);
	}
}
