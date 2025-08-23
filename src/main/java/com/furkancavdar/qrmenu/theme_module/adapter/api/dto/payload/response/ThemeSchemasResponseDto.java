package com.furkancavdar.qrmenu.theme_module.adapter.api.dto.payload.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class ThemeSchemasResponseDto {
    private Integer schemasCount;
    private Map<String, JsonNode> themeSchemas;

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private Map<String, JsonNode> uiSchemas;
}
