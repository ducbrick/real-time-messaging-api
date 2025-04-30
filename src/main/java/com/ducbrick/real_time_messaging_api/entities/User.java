package com.ducbrick.real_time_messaging_api.entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "app_user")
@Builder
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class User {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  private String name;

  private String email;

  @Column(name = "id_provider_url")
  private String idProviderUrl;

  @Column(name = "id_provider_id")
  private String idProviderId;
}
