package com.ducbrick.real_time_messaging_api.dtos;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record MsgToUsr(
	String content
) {
}
