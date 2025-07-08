package com.ducbrick.real_time_messaging_api.dtos;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.util.List;

@Builder
public record MsgHistoryDto(
		List<@NotNull @Valid MsgToUsr> msgs,

		Integer cursor
) {
}
