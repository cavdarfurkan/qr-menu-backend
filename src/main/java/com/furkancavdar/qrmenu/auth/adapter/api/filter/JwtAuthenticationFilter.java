package com.furkancavdar.qrmenu.auth.adapter.api.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.furkancavdar.qrmenu.auth.application.port.out.SessionRepositoryPort;
import com.furkancavdar.qrmenu.auth.application.service.JwtTokenService;
import com.furkancavdar.qrmenu.auth.config.CustomUserDetailsService;
import com.furkancavdar.qrmenu.auth.util.JwtTokenUtil;
import com.furkancavdar.qrmenu.common.ApiResponse;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

  private final JwtTokenUtil jwtTokenUtil;
  private final CustomUserDetailsService customUserDetailsService;
  private final JwtTokenService jwtTokenService;
  private final SessionRepositoryPort sessionRepository;
  private final ObjectMapper jacksonObjectMapper;

  @Qualifier("publicEndpoints")
  private final List<String> publicEndpoints;

  private final AntPathMatcher pathMatcher;

  public JwtAuthenticationFilter(
      JwtTokenUtil jwtTokenUtil,
      CustomUserDetailsService customUserDetailsService,
      JwtTokenService jwtTokenService,
      SessionRepositoryPort sessionRepository,
      ObjectMapper jacksonObjectMapper,
      List<String> publicEndpoints) {
    this.jwtTokenUtil = jwtTokenUtil;
    this.customUserDetailsService = customUserDetailsService;
    this.jwtTokenService = jwtTokenService;
    this.sessionRepository = sessionRepository;
    this.jacksonObjectMapper = jacksonObjectMapper;
    this.publicEndpoints = publicEndpoints;
    this.pathMatcher = new AntPathMatcher();
  }

  @Override
  protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
    String path = request.getRequestURI();
    return publicEndpoints.stream().anyMatch(pattern -> pathMatcher.match(pattern, path));
  }

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {
    String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

    if (authHeader == null || !authHeader.toUpperCase().startsWith("BEARER ")) {
      errorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "Missing required header");
      return;
    }

    String token = authHeader.substring(7);

    String username = jwtTokenUtil.extractUsername(token);
    String sessionId = jwtTokenUtil.extractSessionId(token);
    String accessJti = jwtTokenUtil.extractJti(token);

    if (username == null || sessionId == null || accessJti == null) {
      errorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "Invalid JWT token");
      return;
    }

    UserDetails userDetails = customUserDetailsService.loadUserByUsername(username);

    if (jwtTokenService.isJtiBlacklisted(accessJti)) {
      errorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "JWT token is blacklisted");
      return;
    }

    if (!jwtTokenUtil.validateToken(token, userDetails)) {
      errorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "Invalid user");
      return;
    }

    if (sessionRepository.isSessionExpired(sessionId)) {
      errorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "Session expired");
      return;
    }

    if (!sessionRepository.isUserOwnsSession(username, sessionId)) {
      errorResponse(
          response, HttpServletResponse.SC_UNAUTHORIZED, "Session does not belong to this user");
      return;
    }

    if (SecurityContextHolder.getContext().getAuthentication() == null) {
      //            UsernamePasswordAuthenticationToken authToken =
      // UsernamePasswordAuthenticationToken.unauthenticated(
      //                    userDetails.getUsername(),
      //                    userDetails.getPassword()
      //            );
      UsernamePasswordAuthenticationToken authToken =
          new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
      authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
      SecurityContextHolder.getContext().setAuthentication(authToken);
    }

    filterChain.doFilter(request, response);
  }

  private void errorResponse(HttpServletResponse response, int status, String message)
      throws IOException {
    ApiResponse<Void> apiResponse = ApiResponse.error(message);
    response.setStatus(status);
    response.setContentType("application/json");
    response.setCharacterEncoding("UTF-8");
    jacksonObjectMapper.writeValue(response.getWriter(), apiResponse);
  }
}
