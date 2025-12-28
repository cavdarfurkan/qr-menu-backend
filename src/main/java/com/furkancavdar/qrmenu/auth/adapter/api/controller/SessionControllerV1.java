package com.furkancavdar.qrmenu.auth.adapter.api.controller;

import com.furkancavdar.qrmenu.auth.adapter.api.dto.payload.request.TerminateRequestDto;
import com.furkancavdar.qrmenu.auth.adapter.api.dto.payload.response.ActiveSessionsResponseDto;
import com.furkancavdar.qrmenu.auth.application.exception.SessionOwnerException;
import com.furkancavdar.qrmenu.auth.application.port.in.SessionUseCase;
import com.furkancavdar.qrmenu.auth.domain.SessionMetadata;
import com.furkancavdar.qrmenu.auth.util.JwtTokenUtil;
import com.furkancavdar.qrmenu.common.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/session")
@RequiredArgsConstructor
public class SessionControllerV1 {

  private final SessionUseCase sessionUseCase;
  private final JwtTokenUtil jwtTokenUtil;

  @GetMapping("/active")
  public ResponseEntity<ApiResponse<ActiveSessionsResponseDto>> activeSessions(
      @AuthenticationPrincipal UserDetails userDetails) {
    // TODO: If active sessions are too much (max 10), paginate the results
    List<SessionMetadata> sessionMetadataList =
        sessionUseCase.getActiveSessions(userDetails.getUsername());
    ActiveSessionsResponseDto responseDto = new ActiveSessionsResponseDto(sessionMetadataList);

    return ResponseEntity.ok(ApiResponse.success(responseDto));
  }

  @PostMapping("/terminate")
  public ResponseEntity<ApiResponse<Void>> terminateSession(
      @Valid @RequestBody TerminateRequestDto terminateRequestDto,
      @AuthenticationPrincipal UserDetails userDetails) {
    try {
      sessionUseCase.terminateSession(
          userDetails.getUsername(), terminateRequestDto.getSessionId());
      return ResponseEntity.ok(ApiResponse.success("Session terminated successfully"));
    } catch (SessionOwnerException e) {
      return ResponseEntity.status(HttpStatus.CONFLICT).body(ApiResponse.error(e.getMessage()));
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(ApiResponse.error("An error occurred during terminating session"));
    }
  }

  @PostMapping("/terminate-others")
  public ResponseEntity<ApiResponse<Void>> terminateAllOtherSessions(
      HttpServletRequest request, @AuthenticationPrincipal UserDetails userDetails) {
    try {
      String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

      if (authHeader == null || !authHeader.toUpperCase().startsWith("BEARER ")) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .body(ApiResponse.error("Invalid Bearer token"));
      }

      String token = authHeader.substring(7);
      String currentSessionId = jwtTokenUtil.extractSessionId(token);
      sessionUseCase.terminateAllOtherSessions(userDetails.getUsername(), currentSessionId);
      return ResponseEntity.ok(ApiResponse.success("Sessions terminated successfully"));
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(ApiResponse.error("An error occurred during terminating sessions"));
    }
  }
}
