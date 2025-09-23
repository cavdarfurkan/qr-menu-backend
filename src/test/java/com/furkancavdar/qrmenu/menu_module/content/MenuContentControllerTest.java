package com.furkancavdar.qrmenu.menu_module.content;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.furkancavdar.qrmenu.auth.application.port.out.UserRepositoryPort;
import com.furkancavdar.qrmenu.auth.domain.User;
import com.furkancavdar.qrmenu.menu_module.application.port.out.MenuContentRepositoryPort;
import com.furkancavdar.qrmenu.menu_module.application.port.out.MenuRepositoryPort;
import com.furkancavdar.qrmenu.menu_module.domain.Menu;
import com.furkancavdar.qrmenu.menu_module.domain.MenuContentItem;
import com.furkancavdar.qrmenu.theme_module.application.port.out.ThemeRepositoryPort;
import com.furkancavdar.qrmenu.theme_module.domain.Theme;
import com.furkancavdar.qrmenu.theme_module.domain.ThemeManifest;
import com.redis.testcontainers.RedisContainer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.MediaType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
public class MenuContentControllerTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Container
    @ServiceConnection
    static RedisContainer redis = new RedisContainer("redis:6.2.6").withExposedPorts(6379);

    @Autowired
    MockMvc mvc;
    @Autowired
    private ObjectMapper mapper;
    @Autowired
    private MenuContentRepositoryPort menuContentRepository;
    @Autowired
    private UserRepositoryPort userRepositoryPort;
    @Autowired
    private ThemeRepositoryPort themeRepositoryPort;
    @Autowired
    private MenuRepositoryPort menuRepositoryPort;

    private static String accessToken;
    private static boolean isAuthenticated = false;

    private static User user;
    private static Theme theme;
    private static Menu menu;

    @Test
    public void create_content_with_relation() throws Exception {
        setup();

        // Create a category
        MenuContentItem category = MenuContentItem.builder()
                .menu(menu)
                .ownerId(user.getId())
                .theme(theme)
                .collectionName("category")
                .data(mapper.readTree("{\"name\":\"Coffee\",\"slug\":\"coffee\"}"))
                .build();
        category = menuContentRepository.save(category);

        var body = Map.of(
                "collection", "product",
                "content", Map.of("name", "Espresso", "price", 45),
                "relations", Map.of("category", List.of(category.getId().toString()))
        );

        mvc.perform(post("/api/v1/menu/{menuId}/content", menu.getId())
                        .contentType(MediaType.APPLICATION_JSON.toString())
                        .content(mapper.writeValueAsBytes(body))
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.collection_name", is("product")))
                .andExpect(jsonPath("$.data.resolved.category[0].slug", is("coffee")))
                .andReturn();

//        var id = mapper.readTree(res.getResponse().getContentAsByteArray()).get("data").get("id").asText();
//        mvc.perform(get("/api/v1/menu/{menuId}/content/{collection}/{itemId}", menu.getId(), "product", id)
//                        .header("Authorization", "Bearer " + accessToken))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.success", is(true)))
//                .andExpect(jsonPath("$.message", is("Success")))
//                .andExpect(jsonPath("$.data.name", is("Espresso")))
//                .andExpect(jsonPath("$.data.price", is(45)));
    }

    @Test
    public void update_without_relation() throws Exception {
        setup();

        var createBody = Map.of(
                "collection", "category",
                "content", Map.of("name", "Coffee", "slug", "coffee")
        );

        var res = mvc.perform(post("/api/v1/menu/{menuId}/content", menu.getId())
                        .contentType(MediaType.APPLICATION_JSON.toString())
                        .content(mapper.writeValueAsBytes(createBody))
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.collection_name", is("category")))
                .andReturn();

        var contentId = mapper.readTree(res.getResponse().getContentAsByteArray()).get("data").get("id").asText();

        var updateBody = Map.of(
                "new_content", Map.of("name", "Coffee", "slug", "updated-coffee")
        );

        mvc.perform(put("/api/v1/menu/{menuId}/content/{collection}/{itemId}", menu.getId(), "category", contentId)
                        .contentType(MediaType.APPLICATION_JSON.toString())
                        .content(mapper.writeValueAsBytes(updateBody))
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id", is(contentId)))
                .andExpect(jsonPath("$.data.collection_name", is("category")))
                .andExpect(jsonPath("$.data.data.slug", is("updated-coffee")))
                .andReturn();
    }

    @Test
    public void update_with_relation() throws Exception {
        setup();
        Assertions.fail("Not yet implemented");
    }

    @Test
    public void get_hydrated_collection_content() throws Exception {
        setup();
        Assertions.fail("Not yet implemented");
    }

    @Test
    public void get_hydrated_content() throws Exception {
        setup();

        // Create a category item
        var categoryBody = Map.of(
                "collection", "category",
                "content", Map.of("name", "Coffee", "slug", "coffee")
        );

        var categoryRes = mvc.perform(post("/api/v1/menu/{menuId}/content", menu.getId())
                        .contentType(MediaType.APPLICATION_JSON.toString())
                        .content(mapper.writeValueAsBytes(categoryBody))
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andReturn();

        var categoryId = mapper.readTree(categoryRes.getResponse().getContentAsByteArray()).get("data").get("id").asText();

        // Create a product item
        var productBody = Map.of(
                "collection", "product",
                "content", Map.of("name", "Espresso", "price", 45),
                "relations", Map.of("category", List.of(categoryId))
        );

        var productRes = mvc.perform(post("/api/v1/menu/{menuId}/content", menu.getId())
                        .contentType(MediaType.APPLICATION_JSON.toString())
                        .content(mapper.writeValueAsBytes(productBody))
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andReturn();

        var productId = mapper.readTree(productRes.getResponse().getContentAsByteArray()).get("data").get("id").asText();

        // Test: Get category content by its correct collection
        mvc.perform(get("/api/v1/menu/{menuId}/content/{collection}/{itemId}", menu.getId(), "category", categoryId)
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.data.slug", is("coffee")));

        // Test: Get product content by its correct collection
        mvc.perform(get("/api/v1/menu/{menuId}/content/{collection}/{itemId}", menu.getId(), "product", productId)
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.data.name", is("Espresso")))
                .andExpect(jsonPath("$.data.data.price", is(45)));

        // Test: Try to get category content using wrong collection - should fail
        mvc.perform(get("/api/v1/menu/{menuId}/content/{collection}/{itemId}", menu.getId(), "product", categoryId)
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isNotFound());

        // Test: Try to get product content using wrong collection - should fail
        mvc.perform(get("/api/v1/menu/{menuId}/content/{collection}/{itemId}", menu.getId(), "category", productId)
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isNotFound());
    }

    @DynamicPropertySource
    private static void registerRedisProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", redis::getFirstMappedPort);
    }

    private void authenticate() throws Exception {
        // Register
        var registerBody = Map.of(
                "username", "test_user",
                "email", "a@a.com",
                "password", "123123123"
        );

        var registerRes = mvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON.toString())
                        .content(mapper.writeValueAsBytes(registerBody)))
                .andExpect(status().isCreated())
                .andReturn();

        String username = mapper.readTree(registerRes.getResponse().getContentAsByteArray()).get("data").get("username").asText();
        user = userRepositoryPort.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Login
        var loginBody = Map.of(
                "username", user.getUsername(),
                "password", "123123123"
        );

        var res = mvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON.toString())
                        .content(mapper.writeValueAsBytes(loginBody)))
                .andExpect(status().isOk())
                .andReturn();

        accessToken = mapper.readTree(res.getResponse().getContentAsByteArray()).get("data").get("accessToken").asText();
    }

    private void setup() throws Exception {
        if (isAuthenticated) return;

        authenticate();

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
                mapper.readTree("{\"$ref\":\"#/definitions/product\",\"$schema\":\"http://json-schema.org/draft-07/schema#\",\"definitions\":{\"product\":{\"type\":\"object\",\"required\":[\"name\",\"price\"],\"properties\":{\"name\":{\"type\":\"string\"},\"price\":{\"type\":\"number\"}},\"additionalProperties\":false}}}")
        );
        themeSchemas.put(
                "category",
                mapper.readTree("{\"$ref\":\"#/definitions/category\",\"$schema\":\"http://json-schema.org/draft-07/schema#\",\"definitions\":{\"category\":{\"type\":\"object\",\"required\":[\"slug\",\"name\"],\"properties\":{\"name\":{\"type\":\"string\"},\"slug\":{\"type\":\"string\"}},\"additionalProperties\":false}}}")
        );
        Map<String, JsonNode> uiSchemas = new HashMap<>();

        // Create a theme
        theme = themeRepositoryPort.save(new Theme(user, "thumbnail_url", "location_url", true, themeManifest, themeSchemas, uiSchemas));

        // Create a menu
        menu = menuRepositoryPort.save(new Menu("test menu", user, theme));

        isAuthenticated = true;
    }
}
