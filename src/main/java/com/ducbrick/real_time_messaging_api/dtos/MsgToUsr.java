package com.ducbrick.real_time_messaging_api.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record MsgToUsr(
		@NotNull
		@NotBlank
		String content,

		int senderId,

		int receiverId
) {
}
