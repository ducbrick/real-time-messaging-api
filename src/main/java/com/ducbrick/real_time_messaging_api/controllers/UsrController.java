package com.ducbrick.real_time_messaging_api.controllers;

import com.ducbrick.real_time_messaging_api.dtos.UsrInfoDto;
import com.ducbrick.real_time_messaging_api.exceptions.NoSuchUsrE;
import com.ducbrick.real_time_messaging_api.exceptions.UnauthenticatedE;
import com.ducbrick.real_time_messaging_api.services.UsrService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Validated
public class UsrController {

	private final UsrService usrService;

	@GetMapping("/public/user/info")
	@NotNull
	@Valid
	public UsrInfoDto getUsrInfo(
			@RequestParam(required = false) String id,
			@RequestParam(required = false) String sub,
			@RequestParam(required = false) String issuer
	) throws UnauthenticatedE, NoSuchUsrE {
		if (id != null) {
			return usrService.getUsrInfoById(Integer.parseInt(id));
		}

		if (sub != null && issuer != null) {
			return usrService.getUsrInfoByIssuerAndSub(issuer, sub);
		}

		return usrService.getAuthenticatedUsr();
	}
}
