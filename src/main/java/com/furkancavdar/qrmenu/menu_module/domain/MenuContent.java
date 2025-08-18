package com.furkancavdar.qrmenu.menu_module.domain;

import com.fasterxml.jackson.databind.JsonNode;
import com.furkancavdar.qrmenu.theme_module.domain.Theme;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@ToString
@Builder
public class MenuContent {
    private Long id;
    private final Menu menu;
    private final Long ownerId;
    private final Theme theme;
    private final String collectionName;
    @Setter
    private List<JsonNode> content;

    public MenuContent(Menu menu, Long ownerId, Theme theme, String collectionName, List<JsonNode> content) {
        this.menu = menu;
        this.ownerId = ownerId;
        this.theme = theme;
        this.collectionName = collectionName;
        this.content = content;
    }

    public MenuContent(Long id, Menu menu, Long ownerId, Theme theme, String collectionName, List<JsonNode> content) {
        this(menu, ownerId, theme, collectionName, content);
        this.id = id;
    }
}
