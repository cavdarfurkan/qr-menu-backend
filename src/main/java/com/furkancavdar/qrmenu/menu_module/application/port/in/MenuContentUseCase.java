package com.furkancavdar.qrmenu.menu_module.application.port.in;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.List;

public interface MenuContentUseCase {
    void validateAndSave(String currentUsername, Long menuId, String collection, List<JsonNode> content);

    List<JsonNode> getCollection(String currentUsername, Long menuId, String collection);
}
