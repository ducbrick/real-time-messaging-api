package com.ducbrick.real_time_messaging_api.services.persistence;

import com.ducbrick.real_time_messaging_api.entities.User;
import com.ducbrick.real_time_messaging_api.repos.UserRepo;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Validated
public class UserPersistenceService {
  private final UserRepo userRepo;

  public User getByIssuerAndSub(@NotNull Jwt jwt) {
    Map<String, Object> claims = jwt.getClaims();

    String issuer = (String) claims.getOrDefault("iss", "");
    String sub = (String) claims.getOrDefault("sub", "");

    return userRepo.findByIssuerAndSub(issuer, sub).orElse(null);
  }

  @Transactional
  public User saveNewByJwt(@NotNull @Valid Jwt jwt) {
    Map<String, Object> claims = jwt.getClaims();

    User user = User
        .builder()
        .name((String) claims.getOrDefault("name", "No Name"))
        .email((String) claims.getOrDefault("email", ""))
        .idProviderUrl((String) claims.getOrDefault("iss", ""))
        .idProviderId((String) claims.getOrDefault("sub", ""))
        .build();

    return userRepo.save(user);
  }
}
