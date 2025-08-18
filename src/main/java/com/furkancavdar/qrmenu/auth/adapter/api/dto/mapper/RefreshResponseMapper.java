package com.furkancavdar.qrmenu.auth.adapter.api.dto.mapper;

import com.furkancavdar.qrmenu.auth.adapter.api.dto.payload.response.RefreshResponseDto;
import com.furkancavdar.qrmenu.auth.application.port.in.dto.RefreshResultDto;

public class RefreshResponseMapper {

    private RefreshResponseMapper() {
        // Private constructor to prevent instantiation
    }

    public static RefreshResponseDto fromRefreshResultDto(RefreshResultDto refreshResultDto) {
        return new RefreshResponseDto(refreshResultDto.getJwtTokenPair().getAccessToken());
    }
}
