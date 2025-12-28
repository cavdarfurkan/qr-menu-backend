package com.furkancavdar.qrmenu.auth.application.service;

import com.furkancavdar.qrmenu.auth.application.port.in.AuthenticationUseCase;
import com.furkancavdar.qrmenu.auth.application.port.in.dto.*;
import com.furkancavdar.qrmenu.auth.application.port.in.mapper.RegisterUserDtoMapper;
import com.furkancavdar.qrmenu.auth.application.port.in.mapper.UserDtoMapper;
import com.furkancavdar.qrmenu.auth.application.port.out.RoleRepositoryPort;
import com.furkancavdar.qrmenu.auth.application.port.out.SessionRepositoryPort;
import com.furkancavdar.qrmenu.auth.application.port.out.UserRepositoryPort;
import com.furkancavdar.qrmenu.auth.config.CustomUserDetailsService;
import com.furkancavdar.qrmenu.auth.domain.Role;
import com.furkancavdar.qrmenu.auth.domain.SessionMetadata;
import com.furkancavdar.qrmenu.auth.domain.User;
import com.furkancavdar.qrmenu.auth.util.JwtTokenUtil;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.NotImplementedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthenticationService implements AuthenticationUseCase {

  private static final Logger logger = LoggerFactory.getLogger(AuthenticationService.class);

  private final UserRepositoryPort userRepository;
  private final RoleRepositoryPort roleRepository;
  private final SessionRepositoryPort sessionRepository;

  private final PasswordEncoder passwordEncoder;

  private final AuthenticationManager authenticationManager;
  private final JwtTokenService jwtTokenService;
  private final JwtTokenUtil jwtTokenUtil;
  private final CustomUserDetailsService customUserDetailsService;

  @Override
  public UserDto register(RegisterDto registerDto) {
    logger.info("Registering user: {}", registerDto);
    registerDto.setPassword(passwordEncoder.encode(registerDto.getPassword()));
    User user = RegisterUserDtoMapper.toEntity(registerDto);

    // Set default role to USER
    Role userRole =
        roleRepository
            .findByName("USER")
            .orElseThrow(() -> new RuntimeException("Error: User role not found"));
    user.addRole(userRole);

    User savedUser = userRepository.save(user);
    return UserDtoMapper.toUserDto(savedUser);
  }

  @Override
  public Optional<LoginResultDto> login(LoginDto loginDto) {
    if (loginDto.getUsername() == null || loginDto.getPassword() == null) {
      throw new NullPointerException("Username or password is null");
    }

    try {
      Optional<User> optionalUser = authenticate(loginDto);
      if (optionalUser.isEmpty()) {
        logger.error("User not found: {}", loginDto.getUsername());
        return Optional.empty();
      }
      User user = optionalUser.get();

      String sessionId = generateSessionId();
      JwtTokenPair jwtTokenPair = jwtTokenService.generateTokenPair(user, sessionId);
      String refreshJti = jwtTokenUtil.extractJti(jwtTokenPair.getRefreshToken());

      String ipAddress = loginDto.getIp();
      String userAgentString = loginDto.getUserAgentString();

      SessionMetadata sessionMetadata =
          generateSessionMetadata(
              sessionId,
              user.getUsername(),
              refreshJti,
              ipAddress,
              userAgentString,
              jwtTokenPair.getRefreshToken());

      sessionRepository.saveSession(user.getUsername(), sessionMetadata);

      LoginResultDto loginResultDto =
          new LoginResultDto(UserDtoMapper.toUserDto(optionalUser.get()), jwtTokenPair);
      return Optional.of(loginResultDto);
    } catch (Exception e) {
      logger.error("Authentication failed for user: {}", loginDto.getUsername(), e);
      return Optional.empty();
    }
  }

  @Override
  public String updatePassword(LoginDto loginDto) {
    // TODO
    throw new NotImplementedException();
  }

  @Override
  public void logout() {
    // TODO: Implement custom logout functionality
    throw new NotImplementedException();
  }

  @Override
  public Optional<RefreshResultDto> refreshToken(String refreshToken) {
    if (refreshToken == null) {
      throw new NullPointerException("Refresh token is null");
    }

    try {
      String username = jwtTokenUtil.extractUsername(refreshToken);
      String sessionId = jwtTokenUtil.extractSessionId(refreshToken);
      String refreshJti = jwtTokenUtil.extractJti(refreshToken);
      long expirationTime = jwtTokenUtil.getExpirationTime(refreshToken);

      if (sessionId == null || refreshJti == null) {
        logger.error("Session ID or Refresh Jti not found in refresh token");
        return Optional.empty();
      }

      if (!sessionRepository.isUserOwnsSession(username, sessionId)) {
        logger.error("Session does not belong to user");
        return Optional.empty();
      }

      UserDetails userDetails = customUserDetailsService.loadUserByUsername(username);

      if (jwtTokenService.isJtiBlacklisted(refreshJti)) {
        logger.error("Jti blacklisted");
        sessionRepository.deleteBySessionId(sessionId);
        return Optional.empty();
      }

      if (!jwtTokenUtil.validateToken(refreshToken, userDetails)) {
        logger.error("Refresh token validation failed");
        return Optional.empty();
      }

      if (sessionRepository.isSessionExpired(sessionId)) {
        logger.error("Session has expired");
        sessionRepository.deleteBySessionId(sessionId);
        return Optional.empty();
      }

      jwtTokenService.blacklistJti(
          refreshJti, "JWTs refreshed", (expirationTime + 1000L) - System.currentTimeMillis());

      Optional<User> optionalUser = userRepository.findByUsername(username);
      if (optionalUser.isEmpty()) {
        logger.error("refresh:User not found: {}", username);
        return Optional.empty();
      }

      User user = optionalUser.get();

      JwtTokenPair newJwtTokenPair = jwtTokenService.generateTokenPair(user, sessionId);
      String newRefreshJti = jwtTokenUtil.extractJti(newJwtTokenPair.getRefreshToken());

      Optional<SessionMetadata> optionalSessionMetadata =
          sessionRepository.findBySessionId(sessionId);
      SessionMetadata sessionMetadata =
          optionalSessionMetadata
              .map(
                  metadata ->
                      generateSessionMetadata(
                          metadata.getSessionId(),
                          metadata.getUsername(),
                          newRefreshJti,
                          metadata.getIpAddress(),
                          metadata.getUserAgentString(),
                          newJwtTokenPair.getRefreshToken()))
              .orElseGet(
                  () ->
                      generateSessionMetadata(
                          sessionId,
                          user.getUsername(),
                          newRefreshJti,
                          "",
                          "",
                          newJwtTokenPair.getRefreshToken()));

      // TODO: Check if saveSession also updates the entry
      sessionRepository.saveSession(user.getUsername(), sessionMetadata);

      RefreshResultDto refreshResultDto = new RefreshResultDto(newJwtTokenPair);
      return Optional.of(refreshResultDto);
    } catch (ExpiredJwtException e) {
      logger.error("Refresh token is expired");
      return Optional.empty();
    } catch (MalformedJwtException e) {
      logger.error("Refresh token is malformed");
      return Optional.empty();
    } catch (Exception e) {
      logger.error("Error while refreshing token", e);
      return Optional.empty();
    }
  }

  @Override
  public boolean existsByUsername(String username) {
    logger.info("Checking if user exists with username: {}", username);
    return userRepository.existsByUsername(username);
  }

  @Override
  public boolean existsByEmail(String email) {
    logger.info("Checking if email exists: {}", email);
    return userRepository.existsByEmail(email);
  }

  private String generateSessionId() {
    return UUID.randomUUID().toString();
  }

  private Optional<User> authenticate(LoginDto loginDto) {
    UsernamePasswordAuthenticationToken authToken =
        UsernamePasswordAuthenticationToken.unauthenticated(
            loginDto.getUsername(), loginDto.getPassword());
    Authentication authentication = authenticationManager.authenticate(authToken);

    if (!authentication.isAuthenticated()) {
      throw new BadCredentialsException("Authentication failed");
    }

    SecurityContextHolder.getContext().setAuthentication(authentication);

    return userRepository.findByUsername(loginDto.getUsername());
  }

  private SessionMetadata generateSessionMetadata(
      String sessionId,
      String username,
      String refreshJti,
      String ipAddress,
      String userAgentString,
      String refreshToken) {
    return new SessionMetadata(
        sessionId,
        username,
        refreshJti,
        ipAddress,
        userAgentString,
        System.currentTimeMillis(),
        System.currentTimeMillis(),
        jwtTokenUtil.getExpirationTime(refreshToken));
  }
}
