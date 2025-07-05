package com.ducbrick.real_time_messaging_api.services;

import com.ducbrick.real_time_messaging_api.dtos.UsrInfoDto;
import com.ducbrick.real_time_messaging_api.entities.User;
import com.ducbrick.real_time_messaging_api.exceptions.NoSuchUsrE;
import com.ducbrick.real_time_messaging_api.exceptions.UnauthenticatedE;
import com.ducbrick.real_time_messaging_api.repos.UserRepo;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

@Service
@Validated
@RequiredArgsConstructor
public class UsrService {

	private final UserRepo usrRepo;

	@NotNull
	@Valid
	public UsrInfoDto getUsrInfoById(int id) {
		return usrInfoDtoMapper(
				usrRepo.findById(id)
						.orElseThrow(() -> new NoSuchUsrE("User with id " + id + " doesn't exist"))
		);
	}

	@NotNull
	@Valid
	public UsrInfoDto getUsrInfoByIssuerAndSub(@NotEmpty String issuer, @NotEmpty String sub) {
		return usrInfoDtoMapper(
				usrRepo.findByIssuerAndSub(issuer, sub)
						.orElseThrow(() -> new NoSuchUsrE("User who is subject " + sub + " by issuer " + issuer + " doesn't exist"))
		);
	}

	@NotNull
	@Valid
	public UsrInfoDto getAuthenticatedUsr() throws UnauthenticatedE {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();

		if (auth == null) {
			throw new UnauthenticatedE("Attempting to get self information while being unauthenticated");
		}

		return getUsrInfoById(Integer.parseInt(auth.getName()));
	}

	@NotNull
	@Valid
	private UsrInfoDto usrInfoDtoMapper(@NotNull @Valid User usr) {
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
