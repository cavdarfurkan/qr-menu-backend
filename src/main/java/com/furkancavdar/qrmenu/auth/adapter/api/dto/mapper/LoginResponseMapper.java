package com.furkancavdar.qrmenu.auth.adapter.api.dto.mapper;

import com.furkancavdar.qrmenu.auth.adapter.api.dto.payload.response.LoginResponseDto;
import com.furkancavdar.qrmenu.auth.application.port.in.dto.LoginResultDto;

public class LoginResponseMapper {

    private LoginResponseMapper() {
        // Private constructor to prevent instantiation
    }

    public static LoginResponseDto fromLoginResultDto(LoginResultDto loginResultDto) {
        String accessToken = loginResultDto.getJwtTokenPair().getAccessToken();
        String refreshToken = loginResultDto.getJwtTokenPair().getRefreshToken();
//        return new LoginResponseDto(new JwtTokenPair(accessToken, refreshToken));
        return new LoginResponseDto(accessToken);
    }
}
