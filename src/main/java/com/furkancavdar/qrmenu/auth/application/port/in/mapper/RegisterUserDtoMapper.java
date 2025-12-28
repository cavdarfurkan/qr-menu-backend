package com.furkancavdar.qrmenu.auth.application.port.in.mapper;

import com.furkancavdar.qrmenu.auth.application.port.in.dto.RegisterDto;
import com.furkancavdar.qrmenu.auth.domain.User;

public class RegisterUserDtoMapper {

  private RegisterUserDtoMapper() {
    // Private constructor to prevent instantiation
  }

  public static User toEntity(RegisterDto registerDto) {
    if (registerDto == null) {
      return null;
    }

    return new User(registerDto.getUsername(), registerDto.getPassword(), registerDto.getEmail());
  }
}
