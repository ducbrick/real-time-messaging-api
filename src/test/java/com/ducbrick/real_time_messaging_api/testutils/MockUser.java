package com.ducbrick.real_time_messaging_api.testutils;

import com.ducbrick.real_time_messaging_api.entities.User;

public record MockUser(
		String jwtVal,
		User usr
) {}
