package com.ducbrick.real_time_messaging_api.services.auth;

import com.ducbrick.real_time_messaging_api.wrappers.auth.CustomJwtAuthenticationToken;
import lombok.RequiredArgsConstructor;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimNames;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.util.Assert;

import java.util.Collection;

@RequiredArgsConstructor
public class CustomJwtAuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken> {

  private final Converter<Jwt, Collection<GrantedAuthority>> jwtGrantedAuthoritiesConverter;

  @Override
  public final AbstractAuthenticationToken convert(Jwt jwt) {
    Collection<GrantedAuthority> authorities = this.jwtGrantedAuthoritiesConverter.convert(jwt);

    CustomJwtAuthenticationToken authToken = new CustomJwtAuthenticationToken(jwt, authorities);

    return authToken;
  }
}
