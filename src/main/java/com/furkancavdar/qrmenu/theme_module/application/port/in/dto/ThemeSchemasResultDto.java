package com.furkancavdar.qrmenu.theme_module.application.port.in.dto;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ThemeSchemasResultDto {
    private Integer schemasCount;
    private Map<String, JsonNode> themeSchemas;
}
