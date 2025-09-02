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

import static org.assertj.core.api.Assertions.assertThat;

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

    @DynamicPropertySource
    private static void registerRedisProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", () -> redis.getFirstMappedPort());
    }
}
