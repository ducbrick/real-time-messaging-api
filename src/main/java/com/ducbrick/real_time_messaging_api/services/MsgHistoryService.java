package com.ducbrick.real_time_messaging_api.services;

import com.ducbrick.real_time_messaging_api.dtos.MsgHistoryDto;
import com.ducbrick.real_time_messaging_api.dtos.MsgToUsr;
import com.ducbrick.real_time_messaging_api.entities.Message;
import com.ducbrick.real_time_messaging_api.repos.MsgRepo;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.data.domain.Limit;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@Service
@RequiredArgsConstructor
@Validated
public class MsgHistoryService {

	@Setter
	@Positive
	private int scrollLimit = 2;

	private final MsgRepo msgRepo;

	@Valid
	@NotNull
	public MsgHistoryDto getMsgHistory(Integer cursor, int receiverId) {
		int senderId = Integer.parseInt(SecurityContextHolder.getContext().getAuthentication().getName());

		List<Message> msgs
				= cursor == null
				? msgRepo.getMsgHistory(senderId, receiverId, Limit.of(scrollLimit))
				: msgRepo.getMsgHistory(senderId, receiverId, cursor, Limit.of(scrollLimit));

		if (msgs == null || msgs.isEmpty()) {
			return MsgHistoryDto.builder().cursor(cursor).build();
		}

		return MsgHistoryDto
				.builder()
				.msgs(
						msgs
								.stream()
								.map(
										msgEntity -> MsgToUsr
												.builder()
												.content(msgEntity.getContent())
												.senderId(msgEntity.getSender().getId())
												.receiverId(msgEntity.getSender().getId() ^ senderId ^ receiverId)
												.build()
								)
								.toList()
				)
				.cursor(msgs.getLast().getId())
				.build();
	}
}
