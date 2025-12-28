package com.furkancavdar.qrmenu.menu_module.application.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.furkancavdar.qrmenu.auth.application.port.out.UserRepositoryPort;
import com.furkancavdar.qrmenu.auth.domain.User;
import com.furkancavdar.qrmenu.common.exception.ReferencedItemException;
import com.furkancavdar.qrmenu.common.exception.ResourceNotFoundException;
import com.furkancavdar.qrmenu.menu_module.application.port.in.MenuContentUseCase;
import com.furkancavdar.qrmenu.menu_module.application.port.in.dto.HydratedItemDto;
import com.furkancavdar.qrmenu.menu_module.application.port.out.MenuContentRepositoryPort;
import com.furkancavdar.qrmenu.menu_module.application.port.out.MenuRepositoryPort;
import com.furkancavdar.qrmenu.menu_module.domain.Menu;
import com.furkancavdar.qrmenu.menu_module.domain.MenuContentItem;
import com.furkancavdar.qrmenu.menu_module.domain.MenuContentRelation;
import com.networknt.schema.JsonSchemaException;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.ValidationMessage;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MenuContentService implements MenuContentUseCase {

    private final MenuRepositoryPort menuRepository;
    private final MenuContentRepositoryPort menuContentRepository;
    private final UserRepositoryPort userRepository;

    private final JsonSchemaFactory schemaFactory;

    @Override
    @Transactional
    public HydratedItemDto createContent(String currentUsername, Long menuId, String collection, JsonNode content, Map<String, List<UUID>> relations) {
        ValidateResult validateResult = validateUserAndMenu(currentUsername, menuId, collection);
        User currentUser = validateResult.currentUser;
        Menu menu = validateResult.menu;

        validateJsonWithSchema(collection, content, menu);

        MenuContentItem menuContentItem = MenuContentItem.builder()
                .menu(menu)
                .ownerId(currentUser.getId())
                .theme(menu.getSelectedTheme())
                .collectionName(collection)
                .data(content)
                .build();
        menuContentItem = menuContentRepository.save(menuContentItem);

        if (relations != null && !relations.isEmpty()) {
            for (Map.Entry<String, List<UUID>> entry : relations.entrySet()) {
                String field = entry.getKey();
                List<UUID> targets = entry.getValue() != null ? entry.getValue() : List.<UUID>of();

                validateTargetsBelongToMenu(menuId, new HashSet<>(targets));
                menuContentRepository.deleteBySourceAndField(menuContentItem, field); // replace semantics

                int pos = 0;
                for (UUID targetId : targets) {
                    MenuContentItem targetItem = menuContentRepository.findById(targetId)
                            .orElseThrow(() -> new ResourceNotFoundException("Target content with id %s not found".formatted(targetId)));

                    MenuContentRelation menuContentRelation = MenuContentRelation.builder()
                            .sourceItem(menuContentItem)
                            .fieldName(field)
                            .targetItem(targetItem)
                            .position(pos++)
                            .build();
                    menuContentRepository.save(menuContentRelation);
                }
            }
        }

        return hydrate(menuContentItem.getId());
    }

    @Override
    @Transactional
    public HydratedItemDto updateContent(String currentUsername, Long menuId, String collection, UUID itemId, JsonNode newContent, Map<String, List<UUID>> newRelations) {
        ValidateResult validateResult = validateUserAndMenu(currentUsername, menuId, collection);
        Menu menu = validateResult.menu;

        validateJsonWithSchema(collection, newContent, menu);

        MenuContentItem menuContentItem = menuContentRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Item with id %s not found".formatted(itemId)));

        menuContentItem.setData(newContent);
        menuContentRepository.save(menuContentItem);

        if (newRelations != null && !newRelations.isEmpty()) {
            for (Map.Entry<String, List<UUID>> entry : newRelations.entrySet()) {
                String field = entry.getKey();
                List<UUID> targets = Optional.ofNullable(entry.getValue()).orElse(List.of());

                validateTargetsBelongToMenu(menuId, new HashSet<>(targets));
                menuContentRepository.deleteBySourceAndField(menuContentItem, field);

                int pos = 0;
                for (UUID targetId : targets) {
                    MenuContentItem targetItem = menuContentRepository.findById(targetId)
                            .orElseThrow(() -> new ResourceNotFoundException("Target content with id %s not found".formatted(targetId)));

                    MenuContentRelation menuContentRelation = MenuContentRelation.builder()
                            .sourceItem(menuContentItem)
                            .fieldName(field)
                            .targetItem(targetItem)
                            .position(pos++)
                            .build();
                    menuContentRepository.save(menuContentRelation);
                }
            }
        }

        return hydrate(itemId);
    }

    @Override
    @Transactional
    public List<HydratedItemDto> getCollectionContent(String currentUsername, Long menuId, String collection) {
        validateUserAndMenu(currentUsername, menuId, collection);

        List<MenuContentItem> menuContentItem = menuContentRepository.findByMenuIdAndCollectionName(menuId, collection);
        if (menuContentItem.isEmpty()) {
            throw new ResourceNotFoundException("Content for collection %s not found".formatted(collection));
        }

        return menuContentItem.stream()
                .map(MenuContentItem::getId)
                .map(this::hydrate)
                .toList();
    }

    @Override
    @Transactional
    public HydratedItemDto getContent(String currentUsername, Long menuId, String collection, UUID itemId) {
        ValidateResult validateResult = validateUserAndMenu(currentUsername, menuId, collection);

        MenuContentItem menuContentItem = menuContentRepository.findByMenuIdAndCollectionNameAndId(menuId, collection, itemId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Content with id %s not found in collection %s".formatted(itemId, collection)));

        return hydrate(menuContentItem.getId());
    }

    @Override
    @Transactional
    public void deleteContent(String currentUsername, Long menuId, String collection, UUID itemId) {
        validateUserAndMenu(currentUsername, menuId, collection);

        MenuContentItem menuContentItem = menuContentRepository.findByMenuIdAndCollectionNameAndId(menuId, collection, itemId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Content with id %s not found in collection %s".formatted(itemId, collection)));

        // Check if this item is referenced as a target by other items
        if (menuContentRepository.existsByTargetItemId(itemId)) {
            List<MenuContentRelation> referencingRelations = menuContentRepository.findByTargetItemId(itemId);
            Set<String> referencingSources = referencingRelations.stream()
                    .map(rel -> rel.getSourceItem().getCollectionName() + ":" + rel.getSourceItem().getId())
                    .collect(Collectors.toSet());
            throw new ReferencedItemException(
                    "Cannot delete item %s from collection %s: it is referenced by %d other item(s) (%s)"
                            .formatted(itemId, collection, referencingRelations.size(), 
                                    String.join(", ", referencingSources)));
        }

        // Delete the item (cascade delete will handle relations where this item is the source)
        menuContentRepository.delete(menuContentItem);

        log.info("Deleted menu content item with id {} from collection {}", itemId, collection);
    }

    @Override
    @Transactional
    public void deleteContentBulk(String currentUsername, Long menuId, String collection, List<UUID> itemIds) {
        if (itemIds == null || itemIds.isEmpty()) {
            return;
        }

        validateUserAndMenu(currentUsername, menuId, collection);

        // Remove duplicates from input IDs
        Set<UUID> uniqueIds = new HashSet<>(itemIds);

        // Fetch all items to validate they exist and belong to the correct menu and collection
        List<MenuContentItem> items = menuContentRepository.findAllByIdIn(uniqueIds);

        // Validate all items exist
        if (items.size() != uniqueIds.size()) {
            throw new ResourceNotFoundException("One or more items not found");
        }

        // Validate all items belong to the correct menu and collection
        boolean invalidItem = items.stream()
                .anyMatch(item -> !item.getMenu().getId().equals(menuId) ||
                        !item.getCollectionName().equals(collection));
        if (invalidItem) {
            throw new IllegalArgumentException(
                    "All items must belong to menu %d and collection %s".formatted(menuId, collection));
        }

        // Check if any of the items are referenced as targets by other items (single batch query)
        Set<UUID> referencedItems = menuContentRepository.findReferencedTargetItemIds(uniqueIds);

        if (!referencedItems.isEmpty()) {
            throw new ReferencedItemException(
                    "Cannot delete %d item(s) from collection %s: they are referenced by other items. Referenced items: %s"
                            .formatted(referencedItems.size(), collection, referencedItems));
        }

        // Delete all items in a single batch operation (cascade delete will handle relations where these items are sources)
        menuContentRepository.deleteAll(items);

        log.info("Deleted {} menu content items from collection {}", items.size(), collection);
    }

    @Override
    @Transactional
    public HydratedItemDto hydrate(UUID itemId) {
        MenuContentItem item = menuContentRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Item with id %s not found".formatted(itemId)));

        List<MenuContentRelation> relations = menuContentRepository.findBySourceItemId(itemId);
        Map<String, List<UUID>> byField = relations.stream()
                .collect(Collectors.groupingBy(
                        MenuContentRelation::getFieldName,
                        Collectors.mapping(r -> r.getTargetItem().getId(), Collectors.toList())
                ));

        // Load all target items in a single query
        Set<UUID> allTargetIds = relations.stream()
                .map(r -> r.getTargetItem().getId())
                .collect(Collectors.toSet());
        Map<UUID, JsonNode> targets = menuContentRepository.findAllByIdIn(allTargetIds).stream()
                .collect(Collectors.toMap(MenuContentItem::getId, MenuContentItem::getData));

        // Build resolved map preserving original order (position) per field
        Map<String, List<JsonNode>> resolved = new LinkedHashMap<>();
        for (String field : byField.keySet()) {
            List<MenuContentRelation> ordered = menuContentRepository.findBySourceItemIdAndFieldNameOrderByPositionAsc(itemId, field);
            ArrayList<JsonNode> list = new ArrayList<>(ordered.size());

            for (MenuContentRelation relation : ordered) {
                JsonNode node = targets.get(relation.getTargetItem().getId());
                if (node != null) {
                    list.add(node);
                }
                resolved.put(field, list);
            }
        }

        return new HydratedItemDto(item.getId(), item.getCollectionName(), item.getData(), resolved);
    }

    private void validateJsonWithSchema(String collection, JsonNode content, Menu menu) {
        JsonNode schema = menu.getSelectedTheme().getThemeSchemas().get(collection);
        Set<ValidationMessage> errors = schemaFactory.getSchema(schema).validate(content);
        if (!errors.isEmpty()) {
            throw new JsonSchemaException(errors.toString());
        }
    }

    /**
     * Validate all targets exist and belong to same menu
     *
     * @param menuId    ID of the menu
     * @param targetIds Set of IDs for {@link MenuContentItem}
     */
    private void validateTargetsBelongToMenu(Long menuId, Set<UUID> targetIds) {
        if (targetIds.isEmpty()) {
            return;
        }

        List<MenuContentItem> found = menuContentRepository.findAllByIdIn(targetIds);
        if (found.size() != targetIds.size()) {
            throw new IllegalArgumentException("One or more target items do not exist");
        }

        boolean badMenu = found.stream()
                .anyMatch(item -> !Objects.equals(item.getMenu().getId(), menuId));
        if (badMenu) {
            throw new IllegalArgumentException("Target items must belong to the same menu");
        }
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
