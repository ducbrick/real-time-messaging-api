package com.ducbrick.real_time_messaging_api.services.persistence;

import com.ducbrick.real_time_messaging_api.dtos.AuthServerUsrInfo;
import com.ducbrick.real_time_messaging_api.dtos.UserCredentialDto;
import com.ducbrick.real_time_messaging_api.entities.User;
import com.ducbrick.real_time_messaging_api.repos.UserRepo;
import com.ducbrick.real_time_messaging_api.services.proxies.AuthServerProxy;
import jakarta.transaction.Transactional;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Validated
public class UserPersistenceService {
  private final Logger logger = LoggerFactory.getLogger(UserPersistenceService.class);

  private final AuthServerProxy authServer;
  private final UserRepo userRepo;
  private final Validator validator;

  public Optional<UserCredentialDto> getByIssuerAndSub(@NotNull Jwt jwt) {
    Map<String, Object> claims = jwt.getClaims();

    String issuer = String.valueOf(claims.getOrDefault("iss", ""));
    String sub = String.valueOf(claims.getOrDefault("sub", ""));

    User user = userRepo.findByIssuerAndSub(issuer, sub).orElse(null);

    if (user == null) {
      return Optional.empty();
    }

    //TODO: Update user info after a period of time
    UserCredentialDto userDto = UserCredentialDto
        .builder()
        .id(user.getId())
        .name(user.getName())
        .email(user.getEmail())
        .build();

    return Optional.of(userDto);
  }

  @Transactional
  public UserCredentialDto saveNewByJwt(@NotNull Jwt jwt) {
    AuthServerUsrInfo userInfo = authServer.getUserInfo(jwt.getTokenValue());
    Map<String, Object> claims = jwt.getClaims();

    String name = userInfo.nickname();
    String email = userInfo.email();
    String issuer = String.valueOf(claims.getOrDefault("iss", ""));
    String sub = String.valueOf(claims.getOrDefault("sub", ""));

    User user = User
        .builder()
        .name(name)
        .email(email)
        .idProviderUrl(issuer)
        .idProviderId(sub)
        .build();

    Set<ConstraintViolation<User>> violations = validator.validate(user);

    if (!violations.isEmpty()) {
      logger.error("Constraint violations when saving a new user from JWT: {}", violations);
      throw new ConstraintViolationException(violations);
    }

    user = userRepo.save(user);

    return UserCredentialDto
        .builder()
        .id(user.getId())
        .name(name)
        .email(email)
        .build();
  }
}
