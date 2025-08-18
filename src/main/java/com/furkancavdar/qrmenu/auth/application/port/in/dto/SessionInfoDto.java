package com.furkancavdar.qrmenu.auth.application.port.in.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SessionInfoDto {
    // TODO: Store session sessionId, login time, ip, device info
    // Then rewrite the session related methods @JwtTokenService and @JwtTokenUtil
    private String sessionId;
}
