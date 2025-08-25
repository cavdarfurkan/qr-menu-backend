package com.furkancavdar.qrmenu.menu_module.application.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

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
        // menuContent.setContent(content); // Overrides
        menuContent.getContent().addAll(content); // Appends
        menuContentRepository.save(menuContent);
    }

    @Override
    public List<JsonNode> getCollection(String currentUsername, Long menuId, String collection) {
        User currentUser = userRepository.findByUsername(currentUsername).orElseThrow(
                () -> new ResourceNotFoundException("User not found"));

        Menu menu = menuRepository.findById(menuId)
                .orElseThrow(() -> new ResourceNotFoundException("Menu not found"));
        if (!menu.getOwner().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("Not your menu");
        }

        MenuContent menuContent = menuContentRepository.findByMenuIdAndCollectionName(menuId, collection)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Content for collection %s not found".formatted(collection)));

        return menuContent.getContent();
    }
}
