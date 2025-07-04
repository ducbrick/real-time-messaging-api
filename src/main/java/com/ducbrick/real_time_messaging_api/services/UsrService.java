package com.ducbrick.real_time_messaging_api.services;

import com.ducbrick.real_time_messaging_api.dtos.UsrInfoDto;
import com.ducbrick.real_time_messaging_api.entities.User;
import com.ducbrick.real_time_messaging_api.exceptions.NoSuchUsrE;
import com.ducbrick.real_time_messaging_api.repos.UserRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

@Service
@Validated
@RequiredArgsConstructor
public class UsrService {

	private final UserRepo usrRepo;

	public UsrInfoDto getUsrInfoById(int id) {
		return usrInfoDtoMapper(
				usrRepo.findById(id)
						.orElseThrow(() -> new NoSuchUsrE("User with id " + id + " doesn't exist"))
		);
	}

	public UsrInfoDto getUsrInfoByIssuerAndSub(String issuer, String sub) {
		return null;
	}

	public UsrInfoDto getAuthenticatedUsr() {
		return null;
	}

	private UsrInfoDto usrInfoDtoMapper(User usr) {
		return UsrInfoDto
				.builder()
				.id(usr.getId())
				.name(usr.getName())
				.email(usr.getEmail())
				.issuer(usr.getIdProviderUrl())
				.sub(usr.getIdProviderId())
				.numOfSentMsgs(usrRepo.countSentMsgs(usr.getId()))
				.numOfReceivedMsgs(usrRepo.countReceivedMsgs(usr.getId()))
				.build();
	}
}
