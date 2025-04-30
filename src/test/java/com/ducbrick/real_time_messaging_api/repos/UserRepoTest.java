package com.ducbrick.real_time_messaging_api.repos;

import com.ducbrick.real_time_messaging_api.entities.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class UserRepoTest {
  @Autowired private UserRepo userRepo;

  @DisplayName("Save a new entity to test if the context has been loaded")
  @Test
  public void contextLoad() {
    User user = User
        .builder()
        .name("John Doe")
        .email("jdoe@me.com")
        .idProviderUrl("https://accounts.google.com")
        .idProviderId("123456789")
        .build();

    userRepo.save(user);
  }
  
  @DisplayName("Test entity constraints")
  @Test
  public void testConstraints() {
    User invalidUser = User
        .builder()
        .name("John Doe")
        .email("jdoe@me.com")
        .build();

    assertThatThrownBy(() -> userRepo.save(invalidUser));

    User validUser = User
        .builder()
        .name("John Doe")
        .email("jdoe@me.com")
        .idProviderUrl("https://accounts.google.com")
        .idProviderId("123456789")
        .build();

    validUser = userRepo.save(validUser);
    assertThat(validUser.getId()).isNotNull();
  }
}