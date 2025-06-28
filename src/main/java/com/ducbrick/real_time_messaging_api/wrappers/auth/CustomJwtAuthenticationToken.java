package com.ducbrick.real_time_messaging_api.wrappers.auth;

import com.ducbrick.real_time_messaging_api.dtos.UserDetailsDto;
import com.ducbrick.real_time_messaging_api.entities.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.util.Collection;

public class CustomJwtAuthenticationToken extends JwtAuthenticationToken {
  private UserDetailsDto principal;

  public CustomJwtAuthenticationToken(Jwt jwt) {
    super(jwt);
  }

  public CustomJwtAuthenticationToken(Jwt jwt, Collection<? extends GrantedAuthority> authorities) {
    super(jwt, authorities);
  }

  public CustomJwtAuthenticationToken(Jwt jwt, Collection<? extends GrantedAuthority> authorities, String name) {
    super(jwt, authorities, name);
  }

  public CustomJwtAuthenticationToken(Jwt jwt, Collection<? extends GrantedAuthority> authorities, UserDetailsDto user) {
    super(jwt, authorities);
    principal = user;
  }

  @Override
  public String getName() {
    return principal == null ? null : String.valueOf(principal.id());
  }

  @Override
  public Object getPrincipal() {
    return principal;
  }
}
