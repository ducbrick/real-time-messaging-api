package com.ducbrick.real_time_messaging_api.services;

import com.ducbrick.real_time_messaging_api.dtos.UsrInfoDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

@Service
@Validated
@RequiredArgsConstructor
public class UsrService {

	public UsrInfoDto getUsrInfoById(int id) {
		return null;
	}

	public UsrInfoDto getUsrInfoByIssuerAndSub(String issuer, String sub) {
		return null;
	}

	public UsrInfoDto getAuthenticatedUsr() {
		return null;
	}
}
