package com.ducbrick.real_time_messaging_api.repos;

import com.ducbrick.real_time_messaging_api.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepo extends JpaRepository <User, Integer> {
  @Query("""
      SELECT u
      FROM User u
      WHERE u.idProviderUrl = :issuer AND u.idProviderId = :sub
      """)
  Optional<User> findByIssuerAndSub(@Param("issuer") String issuer, @Param("sub") String sub);

  @Query("""
      SELECT SIZE(u.sentMsgs)
      FROM User u
      WHERE u.id = :id
      """)
  int countSentMsgs(int id);

  @Query("""
      SELECT SIZE(u.receivedMsgs)
      FROM User u
      WHERE u.id = :id
      """)
  int countReceivedMsgs(int id);

}
