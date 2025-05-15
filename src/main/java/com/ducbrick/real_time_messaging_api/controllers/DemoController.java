package com.ducbrick.real_time_messaging_api.controllers;

import com.ducbrick.real_time_messaging_api.entities.User;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DemoController {

	@GetMapping
	public String get(@AuthenticationPrincipal User principal) {
	return "Hello World";
	}

	@PostMapping
	public String post() {
		return "Hello World POST";
	}
}
