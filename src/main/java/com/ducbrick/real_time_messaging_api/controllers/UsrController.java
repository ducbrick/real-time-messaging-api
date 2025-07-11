package com.ducbrick.real_time_messaging_api.controllers;

import com.ducbrick.real_time_messaging_api.dtos.UsrInfoDto;
import com.ducbrick.real_time_messaging_api.exceptions.NoSuchUsrE;
import com.ducbrick.real_time_messaging_api.exceptions.UnauthenticatedE;
import com.ducbrick.real_time_messaging_api.services.UsrService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "User information")
public class UsrController {

	private final UsrService usrService;

	@Operation(
		summary = "Get information about a user"
	)
	@Parameter(
			name = "id",
			in = ParameterIn.QUERY,
			description = "ID of the user to get information about",
			schema = @Schema(type = "string")
	)
	@Parameter(
			name = "sub",
			in = ParameterIn.QUERY,
			description = "OAuth2 subject of the user to get information about",
			schema = @Schema(type = "string")
	)
	@Parameter(
			name = "issuer",
			in = ParameterIn.QUERY,
			description = "OAuth2 issuer of the user to get information about",
			schema = @Schema(type = "string")
	)
	@ApiResponse(
			responseCode = "200",
			description = "Successfully retrieved user information"
	)
	@ApiResponse(
			responseCode = "204",
			description = "Specified user does not exist"
	)
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
