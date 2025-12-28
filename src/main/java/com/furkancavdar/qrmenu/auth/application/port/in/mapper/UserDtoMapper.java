package com.furkancavdar.qrmenu.auth.application.port.in.mapper;

import com.furkancavdar.qrmenu.auth.application.port.in.dto.UserDto;
import com.furkancavdar.qrmenu.auth.domain.Role;
import com.furkancavdar.qrmenu.auth.domain.User;
import java.util.stream.Collectors;

public class UserDtoMapper {

  private UserDtoMapper() {
    // Private constructor to prevent instantiation
  }

  public static UserDto toUserDto(User user) {
    if (user == null) {
      return null;
    }

    UserDto userDto = new UserDto();
    userDto.setId(user.getId());
    userDto.setUsername(user.getUsername());
    userDto.setEmail(user.getEmail());
    userDto.setRoles(user.getRoles().stream().map(Role::name).collect(Collectors.toSet()));
    return userDto;
  }
}
