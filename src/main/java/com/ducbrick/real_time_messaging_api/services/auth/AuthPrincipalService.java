package com.ducbrick.real_time_messaging_api.services.auth;

import com.ducbrick.real_time_messaging_api.dtos.UserCredentialDto;
import com.ducbrick.real_time_messaging_api.exceptions.UnauthenticatedE;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class AuthPrincipalService {
	public UserCredentialDto getPrincipal() throws UnauthenticatedE {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

		UserCredentialDto usr = null;

		try {
			usr = authentication.getPrincipal() == null ? null : (UserCredentialDto) authentication.getPrincipal();
		} catch (ClassCastException e) {
			throw new UnauthenticatedE("Attempting to get self information while being unauthenticated");
		}

		if (usr == null) {
			throw new UnauthenticatedE("Attempting to get self information while being unauthenticated");
		}

		return usr;
	}
}
