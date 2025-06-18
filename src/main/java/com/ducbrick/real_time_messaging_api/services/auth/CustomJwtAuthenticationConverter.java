package com.ducbrick.real_time_messaging_api.services.auth;

import com.ducbrick.real_time_messaging_api.dtos.UserDetailsDto;
import com.ducbrick.real_time_messaging_api.entities.User;
import com.ducbrick.real_time_messaging_api.services.persistence.UserPersistenceService;
import com.ducbrick.real_time_messaging_api.wrappers.auth.CustomJwtAuthenticationToken;
import lombok.RequiredArgsConstructor;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Collection;
import java.util.Optional;

@RequiredArgsConstructor
public class CustomJwtAuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken> {

  private final Converter<Jwt, Collection<GrantedAuthority>> jwtGrantedAuthoritiesConverter;
  private final UserPersistenceService userPersistenceService;

  @Override
  public final AbstractAuthenticationToken convert(Jwt jwt) {
    Collection<GrantedAuthority> authorities = this.jwtGrantedAuthoritiesConverter.convert(jwt);

    Optional<UserDetailsDto> userOpt = userPersistenceService.getByIssuerAndSub(jwt);

    UserDetailsDto user;

	  user = userOpt.orElseGet(() -> userPersistenceService.saveNewByJwt(jwt));

    return new CustomJwtAuthenticationToken(jwt, authorities, user);
  }
}
