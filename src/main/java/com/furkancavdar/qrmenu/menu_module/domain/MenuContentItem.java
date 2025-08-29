package com.furkancavdar.qrmenu.menu_module.domain;

import com.fasterxml.jackson.databind.JsonNode;
import com.furkancavdar.qrmenu.theme_module.domain.Theme;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.UUID;

@Getter
@ToString
@Builder
public class MenuContentItem {
    private UUID id;
    private final Menu menu;
    private final Long ownerId;
    private final Theme theme;
    private final String collectionName;
    @Setter
    private JsonNode data;

    public MenuContentItem(Menu menu, Long ownerId, Theme theme, String collectionName, JsonNode data) {
        this.menu = menu;
        this.ownerId = ownerId;
        this.theme = theme;
        this.collectionName = collectionName;
        this.data = data;
    }

    public MenuContentItem(UUID id, Menu menu, Long ownerId, Theme theme, String collectionName, JsonNode data) {
        this(menu, ownerId, theme, collectionName, data);
        this.id = id;
    }
}
