package com.furkancavdar.qrmenu.auth.adapter.api.dto.mapper;

import com.furkancavdar.qrmenu.auth.adapter.api.dto.payload.request.UpdatePasswordRequestDto;
import com.furkancavdar.qrmenu.auth.application.port.in.dto.UpdatePasswordDto;
import java.util.Objects;

public class UpdatePasswordRequestMapper {

  private UpdatePasswordRequestMapper() {}

  public static UpdatePasswordDto toUpdatePasswordDto(
      String username, UpdatePasswordRequestDto dto) {
    Objects.requireNonNull(username, "Username cannot be null");
    Objects.requireNonNull(dto, "Update password request dto cannot be null");

    return new UpdatePasswordDto(username, dto.getOldPassword(), dto.getNewPassword());
  }
}
