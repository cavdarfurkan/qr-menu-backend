package com.furkancavdar.qrmenu.theme_module.adapter.api.dto.mapper;

import com.furkancavdar.qrmenu.theme_module.adapter.api.dto.payload.response.ThemeSchemasResponseDto;
import com.furkancavdar.qrmenu.theme_module.application.port.in.dto.ThemeSchemasResultDto;

public class ThemeSchemasResponseMapper {

    private ThemeSchemasResponseMapper() {
        // Private constructor to prevent instantiation
    }

    public static ThemeSchemasResponseDto fromThemeSchemasResultDto(ThemeSchemasResultDto themeSchemasResultDto) {
        return new ThemeSchemasResponseDto(themeSchemasResultDto.getSchemasCount(), themeSchemasResultDto.getThemeSchemas());
    }
}
