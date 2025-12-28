package com.furkancavdar.qrmenu.menu_module.content;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.furkancavdar.qrmenu.auth.application.port.out.UserRepositoryPort;
import com.furkancavdar.qrmenu.auth.domain.User;
import com.furkancavdar.qrmenu.menu_module.adapter.api.dto.payload.request.AddMenuContentRequestDto;
import com.furkancavdar.qrmenu.menu_module.application.port.in.MenuContentUseCase;
import com.furkancavdar.qrmenu.menu_module.application.port.in.dto.HydratedItemDto;
import com.furkancavdar.qrmenu.menu_module.application.port.out.MenuContentRepositoryPort;
import com.furkancavdar.qrmenu.menu_module.application.port.out.MenuRepositoryPort;
import com.furkancavdar.qrmenu.menu_module.domain.Menu;
import com.furkancavdar.qrmenu.menu_module.domain.MenuContentItem;
import com.furkancavdar.qrmenu.theme_module.application.port.out.ThemeRepositoryPort;
import com.furkancavdar.qrmenu.theme_module.domain.Theme;
import com.furkancavdar.qrmenu.theme_module.domain.ThemeManifest;
import com.redis.testcontainers.RedisContainer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Testcontainers
@SpringBootTest
@Transactional
public class MenuContentServiceTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Container
    @ServiceConnection
    static RedisContainer redis = new RedisContainer("redis:6.2.6");

    @Autowired
    private MenuContentUseCase menuContentUseCase;
    @Autowired
    private MenuContentRepositoryPort menuContentRepository;
    @Autowired
    private UserRepositoryPort userRepositoryPort;
    @Autowired
    private ThemeRepositoryPort themeRepositoryPort;
    @Autowired
    private MenuRepositoryPort menuRepositoryPort;
    @Autowired
    private ObjectMapper mapper;

    private JsonNode obj(String json) {
        try {
            return mapper.readTree(json);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private User createTestUser(String username, String email) {
        User user = new User(username, "password", email);
        return userRepositoryPort.save(user);
    }

    private ThemeManifest createDefaultThemeManifest() {
        ThemeManifest themeManifest = new ThemeManifest();
        themeManifest.setName("theme");
        themeManifest.setVersion("1.0");
        themeManifest.setDescription("description");
        themeManifest.setAuthor("author");
        themeManifest.setCreatedAt("createdAt");
        return themeManifest;
    }

    private Map<String, JsonNode> createThemeSchemas(String... collections) {
        Map<String, JsonNode> themeSchemas = new HashMap<>();
        for (String collection : collections) {
            if ("product".equals(collection)) {
                themeSchemas.put(
                        "product",
                        obj("{\"$ref\":\"#/definitions/product\",\"$schema\":\"http://json-schema.org/draft-07/schema#\",\"definitions\":{\"product\":{\"type\":\"object\",\"required\":[\"name\",\"price\"],\"properties\":{\"name\":{\"type\":\"string\"},\"price\":{\"type\":\"number\"}},\"additionalProperties\":false}}}")
                );
            } else if ("category".equals(collection)) {
                themeSchemas.put(
                        "category",
                        obj("{\"$ref\":\"#/definitions/category\",\"$schema\":\"http://json-schema.org/draft-07/schema#\",\"definitions\":{\"category\":{\"type\":\"object\",\"required\":[\"name\",\"slug\"],\"properties\":{\"name\":{\"type\":\"string\"},\"slug\":{\"type\":\"string\"}},\"additionalProperties\":false}}}")
                );
            }
        }
        return themeSchemas;
    }

    private Theme createThemeWithSchemas(User owner, String... collections) {
        ThemeManifest manifest = createDefaultThemeManifest();
        Map<String, JsonNode> schemas = createThemeSchemas(collections);
        Map<String, JsonNode> uiSchemas = new HashMap<>();
        Theme theme = new Theme(owner, "thumbnail_url", "location_url", true, manifest, schemas, uiSchemas);
        return themeRepositoryPort.save(theme);
    }

    private Menu createTestMenu(String name, User owner, Theme theme) {
        Menu menu = new Menu(name, owner, theme);
        return menuRepositoryPort.save(menu);
    }

    @Test
    public void create_update_hydrate_flow() {
        // Create an owner
        User owner = new User("username", "password", "email");
        owner = userRepositoryPort.save(owner);

        // Create a theme manifest
        ThemeManifest themeManifest = new ThemeManifest();
        themeManifest.setName("theme");
        themeManifest.setVersion("1.0");
        themeManifest.setDescription("description");
        themeManifest.setAuthor("author");
        themeManifest.setCreatedAt("createdAt");

        // Create a theme schema and ui schema
        Map<String, JsonNode> themeSchemas = new HashMap<>();
        themeSchemas.put(
                "product",
                obj("\"$ref\":\"#/definitions/product\",\"$schema\":\"http://json-schema.org/draft-07/schema#\",\"definitions\":{\"product\":{\"type\":\"object\",\"required\":[\"name\",\"price\"],\"properties\":{\"name\":{\"type\":\"string\"},\"price\":{\"type\":\"number\"}},\"additionalProperties\":false}}")
        );
        Map<String, JsonNode> uiSchemas = new HashMap<>();

        // Create a theme
        Theme theme = new Theme(owner, "thumbnail_url", "location_url", true, themeManifest, themeSchemas, uiSchemas);
        theme = themeRepositoryPort.save(theme);

        // Create a menu
        Menu menu = new Menu("test menu", owner, theme);
        menu = menuRepositoryPort.save(menu);

        // Create a category
        MenuContentItem category = MenuContentItem.builder()
                .menu(menu)
                .ownerId(owner.getId())
                .theme(theme)
                .collectionName("category")
                .data(obj("{\"name\":\"Coffee\",\"slug\":\"coffee\"}"))
                .build();
        category = menuContentRepository.save(category);

        // Create product with relation to category
        AddMenuContentRequestDto req = new AddMenuContentRequestDto(
                "product",
                obj("{\"name\":\"Espresso\",\"price\":45}"),
                Map.of("category", List.of(category.getId()))
        );
        HydratedItemDto created = menuContentUseCase.createContent(
                owner.getUsername(),
                menu.getId(),
                req.getCollection(),
                req.getContent(),
                req.getRelations()
        );

        // Attach relations
        menuContentRepository.findBySourceItemId(created.getId());

        assertThat(created.getCollectionName()).isEqualTo("product");
        assertThat(created.getResolved().get("category")).hasSize(1);
        assertThat(created.getResolved().get("category").get(0).get("slug").asText()).isEqualTo("coffee");

        // Update: replace relation with a new category
        MenuContentItem category2 = MenuContentItem.builder()
                .menu(menu)
                .ownerId(owner.getId())
                .theme(theme)
                .collectionName("category")
                .data(obj("{\"name\":\"Tea\",\"slug\":\"tea\"}"))
                .build();
        category2 = menuContentRepository.save(category2);

        HydratedItemDto updated = menuContentUseCase.updateContent(
                owner.getUsername(),
                menu.getId(),
                created.getCollectionName(),
                created.getId(),
                obj("{\"name\":\"Espresso\",\"price\":50}"),
                Map.of("category", List.of(category2.getId()))
        );

        assertThat(updated.getData().get("price").asInt()).isEqualTo(50);
        assertThat(updated.getResolved().get("category").get(0).get("slug").asText()).isEqualTo("tea");
    }

    @Test
    public void delete_content_should_remove_item_and_relations() {
        User owner = createTestUser("deleteuser", "delete@example.com");
        Theme theme = createThemeWithSchemas(owner, "product");
        Menu menu = createTestMenu("test menu", owner, theme);

        // Create a category
        MenuContentItem category = MenuContentItem.builder()
                .menu(menu)
                .ownerId(owner.getId())
                .theme(theme)
                .collectionName("category")
                .data(obj("{\"name\":\"Drinks\",\"slug\":\"drinks\"}"))
                .build();
        category = menuContentRepository.save(category);

        // Create product with relation to category
        HydratedItemDto created = menuContentUseCase.createContent(
                owner.getUsername(),
                menu.getId(),
                "product",
                obj("{\"name\":\"Latte\",\"price\":60}"),
                Map.of("category", List.of(category.getId()))
        );

        // Verify item exists with relations
        assertThat(menuContentRepository.findById(created.getId())).isPresent();
        assertThat(menuContentRepository.findBySourceItemId(created.getId())).isNotEmpty();

        // Delete the item
        menuContentUseCase.deleteContent(owner.getUsername(), menu.getId(), "product", created.getId());

        // Verify item and its relations are deleted
        assertThat(menuContentRepository.findById(created.getId())).isEmpty();
        assertThat(menuContentRepository.findBySourceItemId(created.getId())).isEmpty();
    }

    @Test
    public void delete_content_bulk_should_remove_multiple_items() {
        User owner = createTestUser("bulkuser", "bulk@example.com");
        Theme theme = createThemeWithSchemas(owner, "product");
        Menu menu = createTestMenu("test menu", owner, theme);

        // Create multiple products
        HydratedItemDto product1 = menuContentUseCase.createContent(
                owner.getUsername(),
                menu.getId(),
                "product",
                obj("{\"name\":\"Coffee\",\"price\":40}"),
                null
        );

        HydratedItemDto product2 = menuContentUseCase.createContent(
                owner.getUsername(),
                menu.getId(),
                "product",
                obj("{\"name\":\"Tea\",\"price\":30}"),
                null
        );

        HydratedItemDto product3 = menuContentUseCase.createContent(
                owner.getUsername(),
                menu.getId(),
                "product",
                obj("{\"name\":\"Juice\",\"price\":50}"),
                null
        );

        // Verify items exist
        assertThat(menuContentRepository.findById(product1.getId())).isPresent();
        assertThat(menuContentRepository.findById(product2.getId())).isPresent();
        assertThat(menuContentRepository.findById(product3.getId())).isPresent();

        // Bulk delete product1 and product2
        menuContentUseCase.deleteContentBulk(
                owner.getUsername(),
                menu.getId(),
                "product",
                List.of(product1.getId(), product2.getId())
        );

        // Verify deleted items are gone
        assertThat(menuContentRepository.findById(product1.getId())).isEmpty();
        assertThat(menuContentRepository.findById(product2.getId())).isEmpty();
        // Verify product3 still exists
        assertThat(menuContentRepository.findById(product3.getId())).isPresent();
    }

    @Test
    public void delete_content_bulk_with_relations_should_cascade() {
        User owner = createTestUser("cascadeuser", "cascade@example.com");
        Theme theme = createThemeWithSchemas(owner, "product");
        Menu menu = createTestMenu("test menu", owner, theme);

        // Create categories
        MenuContentItem category1 = MenuContentItem.builder()
                .menu(menu)
                .ownerId(owner.getId())
                .theme(theme)
                .collectionName("category")
                .data(obj("{\"name\":\"Hot\",\"slug\":\"hot\"}"))
                .build();
        category1 = menuContentRepository.save(category1);

        MenuContentItem category2 = MenuContentItem.builder()
                .menu(menu)
                .ownerId(owner.getId())
                .theme(theme)
                .collectionName("category")
                .data(obj("{\"name\":\"Cold\",\"slug\":\"cold\"}"))
                .build();
        category2 = menuContentRepository.save(category2);

        // Create products with relations
        HydratedItemDto product1 = menuContentUseCase.createContent(
                owner.getUsername(),
                menu.getId(),
                "product",
                obj("{\"name\":\"Cappuccino\",\"price\":55}"),
                Map.of("category", List.of(category1.getId()))
        );

        HydratedItemDto product2 = menuContentUseCase.createContent(
                owner.getUsername(),
                menu.getId(),
                "product",
                obj("{\"name\":\"Iced Coffee\",\"price\":60}"),
                Map.of("category", List.of(category2.getId()))
        );

        // Verify relations exist
        assertThat(menuContentRepository.findBySourceItemId(product1.getId())).hasSize(1);
        assertThat(menuContentRepository.findBySourceItemId(product2.getId())).hasSize(1);

        // Bulk delete both products
        menuContentUseCase.deleteContentBulk(
                owner.getUsername(),
                menu.getId(),
                "product",
                List.of(product1.getId(), product2.getId())
        );

        // Verify items and their relations are deleted
        assertThat(menuContentRepository.findById(product1.getId())).isEmpty();
        assertThat(menuContentRepository.findById(product2.getId())).isEmpty();
        assertThat(menuContentRepository.findBySourceItemId(product1.getId())).isEmpty();
        assertThat(menuContentRepository.findBySourceItemId(product2.getId())).isEmpty();

        // Verify categories still exist (they were targets, not sources)
        assertThat(menuContentRepository.findById(category1.getId())).isPresent();
        assertThat(menuContentRepository.findById(category2.getId())).isPresent();
    }

    @Test
    public void delete_content_should_throw_exception_for_non_existent_item() {
        User owner = createTestUser("erroruser1", "error1@example.com");
        Theme theme = createThemeWithSchemas(owner, "product");
        Menu menu = createTestMenu("test menu", owner, theme);

        // Try to delete non-existent item
        final UUID nonExistentId = UUID.randomUUID();
        final User finalOwner = owner;
        final Menu finalMenu = menu;
        
        assertThatThrownBy(() -> menuContentUseCase.deleteContent(finalOwner.getUsername(), finalMenu.getId(), "product", nonExistentId))
                .isInstanceOf(com.furkancavdar.qrmenu.common.exception.ResourceNotFoundException.class)
                .hasMessageContaining("not found");
    }

    @Test
    public void delete_content_should_throw_exception_for_wrong_collection() {
        User owner = createTestUser("erroruser2", "error2@example.com");
        Theme theme = createThemeWithSchemas(owner, "product");
        Menu menu = createTestMenu("test menu", owner, theme);

        // Create a product
        HydratedItemDto product = menuContentUseCase.createContent(
                owner.getUsername(),
                menu.getId(),
                "product",
                obj("{\"name\":\"Coffee\",\"price\":40}"),
                null
        );

        // Try to delete with wrong collection name
        final User finalOwner = owner;
        final Menu finalMenu = menu;
        assertThatThrownBy(() -> menuContentUseCase.deleteContent(finalOwner.getUsername(), finalMenu.getId(), "category", product.getId()))
                .isInstanceOf(com.furkancavdar.qrmenu.common.exception.ResourceNotFoundException.class)
                .hasMessageContaining("not found");
    }

    @Test
    public void delete_content_bulk_should_handle_empty_list() {
        User owner = createTestUser("emptyuser", "empty@example.com");
        Theme theme = createThemeWithSchemas(owner, "product");
        Menu menu = createTestMenu("test menu", owner, theme);

        // Should not throw exception for empty list
        menuContentUseCase.deleteContentBulk(owner.getUsername(), menu.getId(), "product", List.of());
        
        // Should not throw exception for null list
        menuContentUseCase.deleteContentBulk(owner.getUsername(), menu.getId(), "product", null);
    }

    @Test
    public void delete_content_bulk_should_throw_exception_for_non_existent_items() {
        User owner = createTestUser("bulkerror1", "bulkerror1@example.com");
        Theme theme = createThemeWithSchemas(owner, "product");
        Menu menu = createTestMenu("test menu", owner, theme);

        // Create one product
        HydratedItemDto product = menuContentUseCase.createContent(
                owner.getUsername(),
                menu.getId(),
                "product",
                obj("{\"name\":\"Coffee\",\"price\":40}"),
                null
        );

        // Try to delete with one valid and one non-existent item
        final UUID nonExistentId = UUID.randomUUID();
        final User finalOwner = owner;
        final Menu finalMenu = menu;
        
        assertThatThrownBy(() -> menuContentUseCase.deleteContentBulk(
                finalOwner.getUsername(),
                finalMenu.getId(),
                "product",
                List.of(product.getId(), nonExistentId)
        ))
                .isInstanceOf(com.furkancavdar.qrmenu.common.exception.ResourceNotFoundException.class)
                .hasMessageContaining("One or more items not found");
    }

    @Test
    public void delete_content_bulk_should_throw_exception_for_wrong_collection() {
        User owner = createTestUser("bulkerror2", "bulkerror2@example.com");
        Theme theme = createThemeWithSchemas(owner, "product", "category");
        Menu menu = createTestMenu("test menu", owner, theme);

        // Create products
        HydratedItemDto product1 = menuContentUseCase.createContent(
                owner.getUsername(),
                menu.getId(),
                "product",
                obj("{\"name\":\"Coffee\",\"price\":40}"),
                null
        );

        HydratedItemDto product2 = menuContentUseCase.createContent(
                owner.getUsername(),
                menu.getId(),
                "product",
                obj("{\"name\":\"Tea\",\"price\":30}"),
                null
        );

        // Try to delete with wrong collection name
        final User finalOwner = owner;
        final Menu finalMenu = menu;
        assertThatThrownBy(() -> menuContentUseCase.deleteContentBulk(
                finalOwner.getUsername(),
                finalMenu.getId(),
                "category",
                List.of(product1.getId(), product2.getId())
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("must belong to");
    }

    @Test
    public void delete_content_bulk_should_throw_exception_for_mixed_collections() {
        User owner = createTestUser("bulkerror3", "bulkerror3@example.com");
        Theme theme = createThemeWithSchemas(owner, "product", "category");
        Menu menu = createTestMenu("test menu", owner, theme);

        // Create a product
        HydratedItemDto product = menuContentUseCase.createContent(
                owner.getUsername(),
                menu.getId(),
                "product",
                obj("{\"name\":\"Coffee\",\"price\":40}"),
                null
        );

        // Create a category
        MenuContentItem category = MenuContentItem.builder()
                .menu(menu)
                .ownerId(owner.getId())
                .theme(theme)
                .collectionName("category")
                .data(obj("{\"name\":\"Drinks\",\"slug\":\"drinks\"}"))
                .build();
        category = menuContentRepository.save(category);

        // Try to delete items from different collections using product collection
        final User finalOwner = owner;
        final Menu finalMenu = menu;
        final MenuContentItem finalCategory = category;
        assertThatThrownBy(() -> menuContentUseCase.deleteContentBulk(
                finalOwner.getUsername(),
                finalMenu.getId(),
                "product",
                List.of(product.getId(), finalCategory.getId())
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("must belong to");
    }

    @Test
    public void delete_content_should_throw_exception_when_referenced_as_target() {
        User owner = createTestUser("refuser1", "ref1@example.com");
        Theme theme = createThemeWithSchemas(owner, "product", "category");
        Menu menu = createTestMenu("test menu", owner, theme);

        // Create a category
        HydratedItemDto category = menuContentUseCase.createContent(
                owner.getUsername(),
                menu.getId(),
                "category",
                obj("{\"name\":\"Drinks\",\"slug\":\"drinks\"}"),
                null
        );

        // Create product with relation to category
        HydratedItemDto product = menuContentUseCase.createContent(
                owner.getUsername(),
                menu.getId(),
                "product",
                obj("{\"name\":\"Latte\",\"price\":60}"),
                Map.of("category", List.of(category.getId()))
        );

        // Verify relation exists
        assertThat(menuContentRepository.findByTargetItemId(category.getId())).hasSize(1);

        // Try to delete category (which is referenced by product)
        final User finalOwner = owner;
        final Menu finalMenu = menu;
        assertThatThrownBy(() -> menuContentUseCase.deleteContent(
                finalOwner.getUsername(),
                finalMenu.getId(),
                "category",
                category.getId()
        ))
                .isInstanceOf(com.furkancavdar.qrmenu.common.exception.ReferencedItemException.class)
                .hasMessageContaining("Cannot delete item")
                .hasMessageContaining("it is referenced by");

        // Verify category still exists
        assertThat(menuContentRepository.findById(category.getId())).isPresent();
    }

    @Test
    public void delete_content_bulk_should_throw_exception_when_items_referenced_as_targets() {
        User owner = createTestUser("refuser2", "ref2@example.com");
        Theme theme = createThemeWithSchemas(owner, "product", "category");
        Menu menu = createTestMenu("test menu", owner, theme);

        // Create categories
        HydratedItemDto category1 = menuContentUseCase.createContent(
                owner.getUsername(),
                menu.getId(),
                "category",
                obj("{\"name\":\"Hot\",\"slug\":\"hot\"}"),
                null
        );

        HydratedItemDto category2 = menuContentUseCase.createContent(
                owner.getUsername(),
                menu.getId(),
                "category",
                obj("{\"name\":\"Cold\",\"slug\":\"cold\"}"),
                null
        );

        HydratedItemDto category3 = menuContentUseCase.createContent(
                owner.getUsername(),
                menu.getId(),
                "category",
                obj("{\"name\":\"Desserts\",\"slug\":\"desserts\"}"),
                null
        );

        // Create products with relations to category1 and category2
        HydratedItemDto product1 = menuContentUseCase.createContent(
                owner.getUsername(),
                menu.getId(),
                "product",
                obj("{\"name\":\"Cappuccino\",\"price\":55}"),
                Map.of("category", List.of(category1.getId()))
        );

        HydratedItemDto product2 = menuContentUseCase.createContent(
                owner.getUsername(),
                menu.getId(),
                "product",
                obj("{\"name\":\"Iced Coffee\",\"price\":60}"),
                Map.of("category", List.of(category2.getId()))
        );

        // Verify relations exist
        assertThat(menuContentRepository.findByTargetItemId(category1.getId())).hasSize(1);
        assertThat(menuContentRepository.findByTargetItemId(category2.getId())).hasSize(1);
        assertThat(menuContentRepository.findByTargetItemId(category3.getId())).isEmpty();

        // Try to bulk delete categories (category1 and category2 are referenced, category3 is not)
        final User finalOwner = owner;
        final Menu finalMenu = menu;
        assertThatThrownBy(() -> menuContentUseCase.deleteContentBulk(
                finalOwner.getUsername(),
                finalMenu.getId(),
                "category",
                List.of(category1.getId(), category2.getId(), category3.getId())
        ))
                .isInstanceOf(com.furkancavdar.qrmenu.common.exception.ReferencedItemException.class)
                .hasMessageContaining("Cannot delete")
                .hasMessageContaining("they are referenced by other items");

        // Verify all categories still exist
        assertThat(menuContentRepository.findById(category1.getId())).isPresent();
        assertThat(menuContentRepository.findById(category2.getId())).isPresent();
        assertThat(menuContentRepository.findById(category3.getId())).isPresent();
    }

    @Test
    public void delete_content_should_succeed_when_not_referenced() {
        User owner = createTestUser("refuser3", "ref3@example.com");
        Theme theme = createThemeWithSchemas(owner, "category");
        Menu menu = createTestMenu("test menu", owner, theme);

        // Create a category that is NOT referenced by anything
        HydratedItemDto category = menuContentUseCase.createContent(
                owner.getUsername(),
                menu.getId(),
                "category",
                obj("{\"name\":\"Unused\",\"slug\":\"unused\"}"),
                null
        );

        // Verify it's not referenced
        assertThat(menuContentRepository.findByTargetItemId(category.getId())).isEmpty();

        // Delete should succeed
        menuContentUseCase.deleteContent(owner.getUsername(), menu.getId(), "category", category.getId());

        // Verify it's deleted
        assertThat(menuContentRepository.findById(category.getId())).isEmpty();
    }

    @DynamicPropertySource
    private static void registerRedisProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", () -> redis.getFirstMappedPort());
    }
}
