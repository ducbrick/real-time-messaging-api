package com.ducbrick.real_time_messaging_api.testutils;

import com.ducbrick.real_time_messaging_api.entities.User;
import com.ducbrick.real_time_messaging_api.repos.UserRepo;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;

import java.util.List;
import java.util.Random;

import static org.mockito.Mockito.when;

public class Generator {
	private final static Random random = new Random();

	public static String generateRandomString(int length) {
		return random
				.ints('a', 'z' + 1)
				.limit(length)
				.collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
				.toString();
	}

	public static String generateRandomEmail(int nameLength) {
		return generateRandomString(nameLength) + "@gmail.com";
	}

	public static MockUser generateMockUsr(JwtDecoder mockJwtDecoder, UserRepo usrRepo) {
		String jwtVal = generateRandomString(10);

		User usr = usrRepo.save(User
				.builder()
				.name(generateRandomString(5))
				.email(generateRandomEmail(5))
				.idProviderId(generateRandomString(10))
				.idProviderUrl("https://ducbrick.us.auth0.com")
				.build()
		);

		when(mockJwtDecoder.decode(jwtVal)).thenReturn(
				Jwt
						.withTokenValue(jwtVal)
						.header("alg", "none")
						.issuer(usr.getIdProviderUrl())
						.subject(usr.getIdProviderId())
						.claim("scope", "openid profile email")
						.build());

		return new MockUser(jwtVal, usr);
	}

	public static MockUser generateMockUsr(JwtDecoder mockJwtDecoder, UserRepo usrRepo, List<MockUser> mockUsrs) {
		String jwtVal = generateRandomString(10);

		User usr = usrRepo.save(User
				.builder()
				.name(generateRandomString(5))
				.email(generateRandomEmail(5))
				.idProviderId(generateRandomString(10))
				.idProviderUrl("https://ducbrick.us.auth0.com")
				.build()
		);

		when(mockJwtDecoder.decode(jwtVal)).thenReturn(
				Jwt
						.withTokenValue(jwtVal)
						.header("alg", "none")
						.issuer(usr.getIdProviderUrl())
						.subject(usr.getIdProviderId())
						.claim("scope", "openid profile email")
						.build());

		MockUser mockUser = new MockUser(jwtVal, usr);
		mockUsrs.add(mockUser);
		return mockUser;
	}

}
