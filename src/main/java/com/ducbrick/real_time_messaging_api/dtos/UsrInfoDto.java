package com.ducbrick.real_time_messaging_api.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Builder;

@Builder
public record UsrInfoDto(
		int id,

		@NotBlank
		String name,

		@NotBlank
		@Email
		String email,

		@NotBlank
		String issuer,

		@NotBlank
		String sub,

		@PositiveOrZero
		int numOfSentMsgs,

		@PositiveOrZero
		int numOfReceivedMsgs
) {
}
