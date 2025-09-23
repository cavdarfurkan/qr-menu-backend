package com.furkancavdar.qrmenu.menu_module.application.port.in;

import com.fasterxml.jackson.databind.JsonNode;
import com.furkancavdar.qrmenu.menu_module.application.port.in.dto.HydratedItemDto;
import jakarta.transaction.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface MenuContentUseCase {
    @Transactional
    HydratedItemDto createContent(String currentUsername, Long menuId, String collection, JsonNode content, Map<String, List<UUID>> relations);

    /**
     * @param currentUsername Username of the owner
     * @param menuId          ID of the menu
     * @param collection      Collection name
     * @param itemId          UUID of the content's item
     * @param newContent      New content as JSON
     * @param newRelations    New relations of the content
     * @return {@link JsonNode} - Updated content
     */
    @Transactional
    HydratedItemDto updateContent(String currentUsername, Long menuId, String collection, UUID itemId, JsonNode newContent, Map<String, List<UUID>> newRelations);

    @Transactional
    List<HydratedItemDto> getCollectionContent(String currentUsername, Long menuId, String collection);

    /**
     * Get single menu content
     *
     * @param currentUsername Username of the owner
     * @param menuId          ID of the menu
     * @param collection      Collection name
     * @param itemId          ID of the content's item
     * @return {@link JsonNode}
     */
    @Transactional
    HydratedItemDto getContent(String currentUsername, Long menuId, String collection, UUID itemId);

    @Transactional
    HydratedItemDto hydrate(UUID itemId);
}
