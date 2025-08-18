package com.furkancavdar.qrmenu.auth.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SessionMetadata implements Serializable {
    private String sessionId;
    private String username;
    private String refreshJti;
    private String ipAddress;
    private String userAgentString;
    private Long lastLoginTime;
    private Long createdAt;
    private Long expiresAt;
}