package com.furkancavdar.qrmenu.theme_module.adapter.api.dto.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import com.furkancavdar.qrmenu.auth.domain.User;
import com.furkancavdar.qrmenu.theme_module.adapter.api.dto.payload.request.RegisterThemeRequestDto;
import com.furkancavdar.qrmenu.theme_module.application.port.in.dto.ThemeDto;
import com.furkancavdar.qrmenu.theme_module.domain.ThemeManifest;

import java.util.Map;

public class RegisterThemeRequestMapper {

    private RegisterThemeRequestMapper() {
    }

    public static ThemeDto toThemeDto(Long themeId, RegisterThemeRequestDto dto, User owner, ThemeManifest manifest, Map<String, JsonNode> schemas) {
        return new ThemeDto(
                themeId,
                owner,
                dto.getIsFree(),
                manifest,
                schemas
        );
    }
}
