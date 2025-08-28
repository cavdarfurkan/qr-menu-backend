package com.furkancavdar.qrmenu.menu_module.application.port.in;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.List;

public interface MenuContentUseCase {
    void validateAndSave(String currentUsername, Long menuId, String collection, List<JsonNode> content);

    List<JsonNode> getCollection(String currentUsername, Long menuId, String collection);

    /**
     * Get single menu content
     *
     * @param currentUsername Username of the owner
     * @param menuId          ID of the menu
     * @param collection      Collection name
     * @param itemId          ID of the content's item
     * @return {@link JsonNode}
     */
    JsonNode getContent(String currentUsername, Long menuId, String collection, String itemId);

    /**
     * @param currentUsername Username of the owner
     * @param menuId          ID of the menu
     * @param collection      Collection name
     * @param itemId          ID of the content's item
     * @param newContent      New content as JSON
     * @return {@link JsonNode} - Updated content
     */
    JsonNode updateContent(String currentUsername, Long menuId, String collection, String itemId, JsonNode newContent);
}
