package com.furkancavdar.qrmenu.theme_module.application.port.in.mapper;

import com.furkancavdar.qrmenu.theme_module.application.port.in.dto.ThemeDto;
import com.furkancavdar.qrmenu.theme_module.domain.Theme;

public class ThemeDtoMapper {
    private ThemeDtoMapper() {
    }

    public static ThemeDto toThemeDto(Theme theme) {
        if (theme == null) {
            return null;
        }

        ThemeDto themeDto = new ThemeDto();
        themeDto.setId(theme.getId());
        themeDto.setOwner(theme.getOwner());
        themeDto.setIsFree(theme.isFree());
        themeDto.setThemeManifest(theme.getThemeManifest());
        themeDto.setThemeSchemas(theme.getSchemas());
        return themeDto;
    }

    public static Theme toTheme(ThemeDto themeDto, String thumbnailUrl, String themeLocationUrl) {
        return new Theme(
                themeDto.getId(),
                themeDto.getOwner(),
                thumbnailUrl,
                themeLocationUrl,
                themeDto.getIsFree(),
                themeDto.getThemeManifest(),
                themeDto.getThemeSchemas()
        );
    }
}

