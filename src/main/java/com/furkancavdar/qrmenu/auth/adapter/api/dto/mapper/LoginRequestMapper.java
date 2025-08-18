package com.furkancavdar.qrmenu.auth.adapter.api.dto.mapper;

import com.furkancavdar.qrmenu.auth.adapter.api.dto.payload.request.LoginRequestDto;
import com.furkancavdar.qrmenu.auth.application.port.in.dto.LoginDto;

public class LoginRequestMapper {

    private LoginRequestMapper() {
        // Private constructor to prevent instantiation
    }

    public static LoginDto toLoginDto(LoginRequestDto dto, String ip, String userAgent) {
        return new LoginDto(dto.getUsername(), dto.getPassword(), ip, userAgent);
    }
}
