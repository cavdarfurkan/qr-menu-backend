package com.furkancavdar.qrmenu.menu_module.application.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.furkancavdar.qrmenu.auth.application.port.out.UserRepositoryPort;
import com.furkancavdar.qrmenu.auth.domain.User;
import com.furkancavdar.qrmenu.common.exception.ResourceNotFoundException;
import com.furkancavdar.qrmenu.menu_module.application.port.in.MenuContentUseCase;
import com.furkancavdar.qrmenu.menu_module.application.port.out.MenuContentRepositoryPort;
import com.furkancavdar.qrmenu.menu_module.application.port.out.MenuRepositoryPort;
import com.furkancavdar.qrmenu.menu_module.domain.Menu;
import com.furkancavdar.qrmenu.menu_module.domain.MenuContent;
import com.networknt.schema.JsonSchemaException;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.ValidationMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.OptionalInt;
import java.util.Set;
import java.util.stream.IntStream;

@Slf4j
@Service
@RequiredArgsConstructor
public class MenuContentService implements MenuContentUseCase {

    private final MenuRepositoryPort menuRepository;
    private final MenuContentRepositoryPort menuContentRepository;
    private final UserRepositoryPort userRepository;

    private final JsonSchemaFactory schemaFactory;

    @Override
    public void validateAndSave(String currentUsername, Long menuId, String collection, List<JsonNode> content) {
        ValidateResult validateResult = validateUserAndMenu(currentUsername, menuId, collection);
        User currentUser = validateResult.currentUser;
        Menu menu = validateResult.menu;

        JsonNode schema = menu.getSelectedTheme().getThemeSchemas().get(collection);
        content.forEach((data) -> {
            Set<ValidationMessage> errors = schemaFactory.getSchema(schema).validate(data);
            if (!errors.isEmpty()) {
                throw new JsonSchemaException(errors.toString());
            }
        });

        MenuContent menuContent = menuContentRepository.findByMenuIdAndCollectionName(menuId, collection)
                .orElseGet(() -> MenuContent.builder()
                        .menu(menu)
                        .ownerId(currentUser.getId())
                        .theme(menu.getSelectedTheme())
                        .collectionName(collection)
                        .content(new ArrayList<>())
                        .build());
        menuContent.getContent().addAll(content); // Appends
        menuContentRepository.save(menuContent);
    }

    @Override
    public List<JsonNode> getCollection(String currentUsername, Long menuId, String collection) {
        validateUserAndMenu(currentUsername, menuId, collection);

        MenuContent menuContent = menuContentRepository.findByMenuIdAndCollectionName(menuId, collection)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Content for collection %s not found".formatted(collection)));

        return menuContent.getContent();
    }

    @Override
    public JsonNode getContent(String currentUsername, Long menuId, String collection, String itemId) {
        ValidateResult validateResult = validateUserAndMenu(currentUsername, menuId, collection);
        Menu menu = validateResult.menu;

        MenuContent menuContent = menuContentRepository.findByMenuIdAndCollectionName(menuId, collection)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Content for collection %s not found".formatted(collection)));

        int contentIndex = findContentIndex(menu, menuContent, collection, itemId);

        return menuContent.getContent().get(contentIndex);
    }

    @Override
    public JsonNode updateContent(String currentUsername, Long menuId, String collection, String itemId, JsonNode newContent) {
        ValidateResult validateResult = validateUserAndMenu(currentUsername, menuId, collection);
        Menu menu = validateResult.menu;

        JsonNode schema = menu.getSelectedTheme().getThemeSchemas().get(collection);
        Set<ValidationMessage> errors = schemaFactory.getSchema(schema).validate(newContent);
        if (!errors.isEmpty()) {
            throw new JsonSchemaException(errors.toString());
        }

        MenuContent menuContent = menuContentRepository.findByMenuIdAndCollectionName(menuId, collection)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Content for collection %s not found".formatted(collection)));

        int contentIndex = findContentIndex(menu, menuContent, collection, itemId);

        List<JsonNode> contents = menuContent.getContent();
        contents.set(contentIndex, newContent);

        menuContent.setContent(contents); // Overrides
        menuContentRepository.save(menuContent);

        return contents.get(contentIndex);
    }

    private int findContentIndex(Menu menu, MenuContent menuContent, String collection, String itemId) throws IllegalArgumentException, ResourceNotFoundException {
        // Ensure 'id' field exists in the schema
        JsonNode selectedThemeSchema = menu.getSelectedTheme().getThemeSchemas().get(collection);
        if (!selectedThemeSchema.get("definitions").get(collection).get("properties").has("id")) {
            throw new IllegalArgumentException("Schema does not contain 'id' field.");
        }

        OptionalInt indexOpt = IntStream.range(0, menuContent.getContent().size())
                .filter(i -> {
                    String id = menuContent.getContent().get(i).get("id").asText();
                    return id.equals(itemId);
                })
                .findFirst();

        if (indexOpt.isEmpty()) {
            throw new ResourceNotFoundException("Content item not found");
        }

        return indexOpt.getAsInt();
    }

    private ValidateResult validateUserAndMenu(String currentUsername, Long menuId, String collection) throws ResourceNotFoundException, AccessDeniedException {
        User currentUser = userRepository.findByUsername(currentUsername).orElseThrow(
                () -> new ResourceNotFoundException("User not found"));

        Menu menu = menuRepository.findById(menuId)
                .orElseThrow(() -> new ResourceNotFoundException("Menu not found"));
        if (!menu.getOwner().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("Not your menu");
        }

        Long selectedThemeId = menu.getSelectedTheme().getId();
        if (selectedThemeId == null) {
            throw new RuntimeException("Menu has no theme selected");
        }

        if (!menu.getSelectedTheme().getThemeSchemas().containsKey(collection)) {
            throw new ResourceNotFoundException("Schema for collection %s not found".formatted(collection));
        }

        return new ValidateResult(currentUser, menu);
    }

    private record ValidateResult(User currentUser, Menu menu) {
    }
}
