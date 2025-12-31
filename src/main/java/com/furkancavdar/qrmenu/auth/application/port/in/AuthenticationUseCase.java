package com.furkancavdar.qrmenu.auth.application.port.in;

import com.furkancavdar.qrmenu.auth.application.port.in.dto.*;
import java.util.Optional;

public interface AuthenticationUseCase {
  UserDto register(RegisterDto registerDto);

  Optional<LoginResultDto> login(LoginDto loginDto);

  boolean updatePassword(UpdatePasswordDto updatePasswordDto);

  Optional<RefreshResultDto> refreshToken(String refreshToken);

  boolean existsByUsername(String username);

  boolean existsByEmail(String email);
}
