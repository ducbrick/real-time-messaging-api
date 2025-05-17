package com.ducbrick.real_time_messaging_api.dtos;

import lombok.Builder;

@Builder
public record UserDetailsDto(
    Integer id,
    String name,
    String email
) {
}
