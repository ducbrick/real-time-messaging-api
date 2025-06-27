package com.ducbrick.real_time_messaging_api.dtos;

public record MsgFromUsr(
	String receiver,
	String content
) {
}
