package com.furkancavdar.qrmenu.theme_module.application.port.in.dto;

import com.fasterxml.jackson.databind.JsonNode;
import com.furkancavdar.qrmenu.auth.domain.User;
import com.furkancavdar.qrmenu.theme_module.domain.ThemeManifest;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ThemeDto {
    private Long id;
    private User owner;
    private Boolean isFree = Boolean.TRUE;
    private ThemeManifest themeManifest;
    private Map<String, JsonNode> themeSchemas;
}
