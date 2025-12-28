package com.furkancavdar.qrmenu.auth.adapter.api.controller;

import com.furkancavdar.qrmenu.auth.adapter.api.dto.mapper.LoginRequestMapper;
import com.furkancavdar.qrmenu.auth.adapter.api.dto.mapper.LoginResponseMapper;
import com.furkancavdar.qrmenu.auth.adapter.api.dto.mapper.RefreshResponseMapper;
import com.furkancavdar.qrmenu.auth.adapter.api.dto.payload.request.LoginRequestDto;
import com.furkancavdar.qrmenu.auth.adapter.api.dto.payload.response.LoginResponseDto;
import com.furkancavdar.qrmenu.auth.adapter.api.dto.payload.response.RefreshResponseDto;
import com.furkancavdar.qrmenu.auth.application.port.in.AuthenticationUseCase;
import com.furkancavdar.qrmenu.auth.application.port.in.dto.LoginResultDto;
import com.furkancavdar.qrmenu.auth.application.port.in.dto.RefreshResultDto;
import com.furkancavdar.qrmenu.auth.application.port.in.dto.RegisterDto;
import com.furkancavdar.qrmenu.auth.application.port.in.dto.UserDto;
import com.furkancavdar.qrmenu.common.ApiResponse;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthControllerV1 {

  @Value("${spring.security.token.refresh.expiration}")
  private long REFRESH_EXPIRATION;

  @Value("${app.cookie.secure}")
  private boolean secureCookie;

  private final AuthenticationUseCase authenticationUseCase;

  @PostMapping("/register")
  public ResponseEntity<ApiResponse<UserDto>> registerUser(
      @Valid @RequestBody RegisterDto registerDto) {
    if (authenticationUseCase.existsByUsername(registerDto.getUsername())) {
      return ResponseEntity.badRequest().body(ApiResponse.error("Username is already taken!"));
    }

    if (authenticationUseCase.existsByEmail(registerDto.getEmail())) {
      return ResponseEntity.badRequest().body(ApiResponse.error("Email is already in use!"));
    }

    UserDto registeredUser = authenticationUseCase.register(registerDto);
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(ApiResponse.success("User registered successfully!", registeredUser));
  }

  @PostMapping("/login")
  public ResponseEntity<ApiResponse<LoginResponseDto>> loginUser(
      @Valid @RequestBody LoginRequestDto loginRequestDto,
      HttpServletRequest request,
      HttpServletResponse response) {
    String ip = request.getHeader("X-FORWARDED-FOR");
    if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
      ip = request.getRemoteAddr();
    }
    String userAgentString = request.getHeader("User-Agent");

    Optional<LoginResultDto> optionalLoginResult =
        authenticationUseCase.login(
            LoginRequestMapper.toLoginDto(loginRequestDto, ip, userAgentString));
    if (optionalLoginResult.isEmpty()) {
      log.info("Login failed!");
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
          .body(ApiResponse.error("Login failed!"));
    }

    LoginResponseDto loginResponseDto =
        LoginResponseMapper.fromLoginResultDto(optionalLoginResult.get());
    String refreshTokenCookie =
        buildRefreshTokenCookie(optionalLoginResult.get().getJwtTokenPair().getRefreshToken());

    return ResponseEntity.ok()
        .header(HttpHeaders.SET_COOKIE, refreshTokenCookie)
        .body(ApiResponse.success("Login successful", loginResponseDto));
  }

  //    @GetMapping("/logout")
  //    public ResponseEntity<ApiResponse<Void>> logout(HttpServletRequest request,
  // HttpServletResponse response) {
  //        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
  //        if (auth != null) {
  //            new SecurityContextLogoutHandler().logout(request, response, auth);
  //        }
  //
  //        authenticationUseCase.logout();
  //
  //        return ResponseEntity.ok(ApiResponse.success("Logout successful"));
  //    }

  @PostMapping("/refresh")
  public ResponseEntity<ApiResponse<RefreshResponseDto>> refreshToken(
      HttpServletRequest request, HttpServletResponse response) {
    log.info("auth controller v1: refresh");
    Cookie[] cookies = request.getCookies();
    if (cookies == null) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
          .body(ApiResponse.error("Refresh token cookie is missing"));
    }

    String refreshToken = null;
    for (Cookie cookie : cookies) {
      if (cookie.getName().equals("refreshToken")) {
        refreshToken = cookie.getValue();
        break;
      }
    }
    log.info("Refresh Token: {}", refreshToken);

    if (refreshToken == null) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
          .body(ApiResponse.error("Refresh token cookie is missing"));
    }

    Optional<RefreshResultDto> optionalRefreshResultDto =
        authenticationUseCase.refreshToken(refreshToken);
    if (optionalRefreshResultDto.isEmpty()) {
      log.info("Refresh failed!");
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
          .body(ApiResponse.error("Refresh failed!"));
    }

    RefreshResponseDto refreshResponseDto =
        RefreshResponseMapper.fromRefreshResultDto(optionalRefreshResultDto.get());
    String refreshTokenCookie =
        buildRefreshTokenCookie(optionalRefreshResultDto.get().getJwtTokenPair().getRefreshToken());

    return ResponseEntity.ok()
        .header(HttpHeaders.SET_COOKIE, refreshTokenCookie)
        .body(ApiResponse.success("Refresh successful", refreshResponseDto));
  }

  @GetMapping("/csrf")
  public CsrfToken getCsrfToken(CsrfToken csrfToken) {
    return csrfToken;
  }

  private String buildRefreshTokenCookie(String token) {
    long refreshTokenExpiration = TimeUnit.MILLISECONDS.toSeconds(REFRESH_EXPIRATION);
    ResponseCookie refreshTokenCookie =
        ResponseCookie.from("refreshToken", token)
            .httpOnly(true)
            .secure(secureCookie)
            .path("/api/v1/auth/refresh")
            .sameSite("Lax")
            .maxAge(refreshTokenExpiration)
            .build();
    return refreshTokenCookie.toString();
  }
}
