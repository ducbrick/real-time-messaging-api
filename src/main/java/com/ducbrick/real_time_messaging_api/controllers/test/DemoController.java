package com.ducbrick.real_time_messaging_api.controllers.test;

import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

// TODO: Set this to TEST profile
@RestController
@Profile("test")
public class DemoController {

	@GetMapping
	public String get() {
	return "Hello World";
	}

	@PostMapping
	public String post() {
		return "Hello World POST";
	}
}
