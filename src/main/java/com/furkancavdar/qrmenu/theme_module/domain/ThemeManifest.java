package com.furkancavdar.qrmenu.theme_module.domain;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.Embeddable;
import lombok.Data;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.List;

@Data
@Embeddable
public class ThemeManifest {
    private String name;
    private String version;
    private String description;
    private String author;
    private String createdAt;

    @JdbcTypeCode(SqlTypes.JSON)
    private List<JsonNode> schemasLocation;
}
