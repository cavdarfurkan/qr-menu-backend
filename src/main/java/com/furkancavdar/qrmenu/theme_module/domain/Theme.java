package com.furkancavdar.qrmenu.theme_module.domain;

import com.fasterxml.jackson.databind.JsonNode;
import com.furkancavdar.qrmenu.auth.domain.User;
import com.furkancavdar.qrmenu.theme_module.domain.ThemeManifest;
import lombok.Getter;
import lombok.ToString;

import java.util.Map;

@Getter
@ToString
public class Theme {
    private Long id;
    private final User owner;
    private final String thumbnailUrl;
    private final String themeLocationUrl;
    private final boolean isFree;
    private final ThemeManifest themeManifest;
    private final Map<String, JsonNode> schemas;

    public Theme(User owner, String thumbnailUrl, String themeLocationUrl, boolean isFree, ThemeManifest themeManifest, Map<String, JsonNode> schemas) {
        this.owner = owner;
        this.thumbnailUrl = thumbnailUrl;
        this.themeLocationUrl = themeLocationUrl;
        this.isFree = isFree;
        this.themeManifest = themeManifest;
        this.schemas = schemas;
    }

    public Theme(Long id, User owner, String thumbnailUrl, String themeLocationUrl, boolean isFree, ThemeManifest themeManifest, Map<String, JsonNode> schemas) {
        this(owner, thumbnailUrl, themeLocationUrl, isFree, themeManifest, schemas);
        this.id = id;
    }

    public Boolean isOwner(String username) {
        return owner.getUsername().equals(username);
    }
}