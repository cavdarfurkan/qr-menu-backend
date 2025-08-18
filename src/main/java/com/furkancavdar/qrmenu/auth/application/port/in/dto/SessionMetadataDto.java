package com.furkancavdar.qrmenu.auth.application.port.in.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SessionMetadataDto implements Serializable {
    private String sessionId;
    private Long userId;
    private String refreshJti;
    private String ipAddress;
    private String userAgentString;
    private String lastLoginTime;
    private String createdAt;
}
