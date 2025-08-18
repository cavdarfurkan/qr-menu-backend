package com.furkancavdar.qrmenu.auth.application.port.in;

import com.furkancavdar.qrmenu.auth.application.port.in.dto.*;

import java.util.Optional;

public interface AuthenticationUseCase {
    UserDto register(RegisterDto registerDto);

    Optional<LoginResultDto> login(LoginDto loginDto);

    String updatePassword(LoginDto loginDto);

    void logout();

    Optional<RefreshResultDto> refreshToken(String refreshToken);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);
}