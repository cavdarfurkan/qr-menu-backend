package com.furkancavdar.qrmenu.auth.application.port.in.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginResultDto {
  private UserDto user;
  private JwtTokenPair jwtTokenPair;
}
