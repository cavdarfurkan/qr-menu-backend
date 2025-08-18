package com.furkancavdar.qrmenu.theme_module.application.port.in.dto;

import com.furkancavdar.qrmenu.theme_module.domain.ThemeManifest;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ThemeManifestResultDto {
    private ThemeManifest themeManifest;
}
