package com.ducbrick.real_time_messaging_api.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
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

  @NotBlank
  private String name;

  @NotBlank
  private String email;

  @Column(name = "id_provider_url")
  @NotBlank
  private String idProviderUrl;

  @Column(name = "id_provider_id")
  @NotBlank
  private String idProviderId;
}
