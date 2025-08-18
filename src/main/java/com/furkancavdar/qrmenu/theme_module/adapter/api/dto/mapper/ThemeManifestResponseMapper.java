package com.furkancavdar.qrmenu.theme_module.adapter.api.dto.mapper;

import com.furkancavdar.qrmenu.theme_module.adapter.api.dto.payload.response.ThemeManifestResponseDto;
import com.furkancavdar.qrmenu.theme_module.application.port.in.dto.ThemeManifestResultDto;

public class ThemeManifestResponseMapper {

    private ThemeManifestResponseMapper() {
        // Private constructor to prevent instantiation
    }

    public static ThemeManifestResponseDto fromThemeManifestResultDto(ThemeManifestResultDto themeManifestResultDto) {
        return new ThemeManifestResponseDto(themeManifestResultDto.getThemeManifest());
    }
}
