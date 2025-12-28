package com.furkancavdar.qrmenu.auth.adapter.api.controller;

import com.furkancavdar.qrmenu.auth.adapter.persistence.repository.UserAdapter;
import com.furkancavdar.qrmenu.auth.domain.User;
import com.furkancavdar.qrmenu.common.ApiResponse;
import jakarta.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/test")
public class AuthTestController {

  private static final Logger log = LoggerFactory.getLogger(AuthTestController.class);
  private final UserAdapter userAdapter;

  public AuthTestController(UserAdapter userAdapter) {
    this.userAdapter = userAdapter;
  }

  @GetMapping("/all")
  public ApiResponse<String> allAccess() {
    return ApiResponse.success("Public Content");
  }

  @GetMapping("/whoami")
  public ApiResponse<String> whoamiAccess(@AuthenticationPrincipal UserDetails userDetails) {
    Optional<User> user = userAdapter.findByUsername(userDetails.getUsername());
    if (user.isEmpty()) {
      return ApiResponse.error("User not found");
    }
    return ApiResponse.success(user.get().toString());
  }

  @GetMapping("/user")
  @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
  public ApiResponse<Map<String, String>> userAccess(
      @AuthenticationPrincipal UserDetails userDetails) {
    log.info(userDetails.getUsername());
    Map<String, String> response = new HashMap<>();
    response.put("message", "User Content");
    response.put("username", userDetails.getUsername());
    return ApiResponse.success(response);
  }

  @GetMapping("/admin")
  @PreAuthorize("hasRole('ADMIN')")
  public ApiResponse<String> adminAccess() {
    // TODO: If has not role, return with appropriate response (maybe define a custom exception)
    return ApiResponse.success("Admin Content");
  }

  @GetMapping("/session")
  public ApiResponse<Map<String, Object>> testSession(HttpSession session) {
    Integer counter = (Integer) session.getAttribute("counter");
    if (counter == null) {
      counter = 1;
    } else {
      counter++;
    }
    session.setAttribute("counter", counter);

    Map<String, Object> sessionInfo = new HashMap<>();
    sessionInfo.put("sessionId", session.getId());
    sessionInfo.put("counter", counter);
    sessionInfo.put("creationTime", session.getCreationTime());
    sessionInfo.put("lastAccessedTime", session.getLastAccessedTime());
    sessionInfo.put("maxInactiveInterval", session.getMaxInactiveInterval());

    return ApiResponse.success("Session info retrieved", sessionInfo);
  }
}
