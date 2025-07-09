package com.ducbrick.real_time_messaging_api.controllers.advices;

import com.ducbrick.real_time_messaging_api.exceptions.NoSuchUsrE;
import com.ducbrick.real_time_messaging_api.exceptions.UnauthenticatedE;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ExHandlerAdvice {

	@ExceptionHandler(UnauthenticatedE.class)
	public ResponseEntity<String> handleEx(UnauthenticatedE e) {
		return ResponseEntity.badRequest().body(e.getMessage());
	}

	@ExceptionHandler(NoSuchUsrE.class)
	public ResponseEntity<String> handleEx(NoSuchUsrE e) {
		return ResponseEntity.noContent().build();
	}
}
