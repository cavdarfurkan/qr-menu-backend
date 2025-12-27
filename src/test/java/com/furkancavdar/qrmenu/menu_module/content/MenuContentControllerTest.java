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

        // Create first category
        MenuContentItem category1 = MenuContentItem.builder()
                .menu(menu)
                .ownerId(user.getId())
                .theme(theme)
                .collectionName("category")
                .data(mapper.readTree("{\"name\":\"Hot Drinks\",\"slug\":\"hot-drinks\"}"))
                .build();
        category1 = menuContentRepository.save(category1);

        // Create second category
        MenuContentItem category2 = MenuContentItem.builder()
                .menu(menu)
                .ownerId(user.getId())
                .theme(theme)
                .collectionName("category")
                .data(mapper.readTree("{\"name\":\"Cold Drinks\",\"slug\":\"cold-drinks\"}"))
                .build();
        category2 = menuContentRepository.save(category2);

        // Create a product with relation to first category
        var createBody = Map.of(
                "collection", "product",
                "content", Map.of("name", "Espresso", "price", 45),
                "relations", Map.of("category", List.of(category1.getId().toString()))
        );

        var createRes = mvc.perform(post("/api/v1/menu/{menuId}/content", menu.getId())
                        .contentType(MediaType.APPLICATION_JSON.toString())
                        .content(mapper.writeValueAsBytes(createBody))
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.collection_name", is("product")))
                .andExpect(jsonPath("$.data.resolved.category[0].slug", is("hot-drinks")))
                .andReturn();

        var productId = mapper.readTree(createRes.getResponse().getContentAsByteArray()).get("data").get("id").asText();

        // Update the product: change price AND switch to second category
        var updateBody = Map.of(
                "new_content", Map.of("name", "Espresso", "price", 50),
                "relations", Map.of("category", List.of(category2.getId().toString()))
        );

        mvc.perform(put("/api/v1/menu/{menuId}/content/{collection}/{itemId}", menu.getId(), "product", productId)
                        .contentType(MediaType.APPLICATION_JSON.toString())
                        .content(mapper.writeValueAsBytes(updateBody))
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id", is(productId)))
                .andExpect(jsonPath("$.data.collection_name", is("product")))
                .andExpect(jsonPath("$.data.data.price", is(50)))
                .andExpect(jsonPath("$.data.resolved.category[0].slug", is("cold-drinks")))
                .andReturn();
    }

    @Test
    public void get_hydrated_collection_content() throws Exception {
        setup();

        // Create categories
        MenuContentItem category1 = MenuContentItem.builder()
                .menu(menu)
                .ownerId(user.getId())
                .theme(theme)
                .collectionName("category")
                .data(mapper.readTree("{\"name\":\"Hot Drinks\",\"slug\":\"hot-drinks\"}"))
                .build();
        category1 = menuContentRepository.save(category1);

        MenuContentItem category2 = MenuContentItem.builder()
                .menu(menu)
                .ownerId(user.getId())
                .theme(theme)
                .collectionName("category")
                .data(mapper.readTree("{\"name\":\"Cold Drinks\",\"slug\":\"cold-drinks\"}"))
                .build();
        category2 = menuContentRepository.save(category2);

        MenuContentItem category3 = MenuContentItem.builder()
                .menu(menu)
                .ownerId(user.getId())
                .theme(theme)
                .collectionName("category")
                .data(mapper.readTree("{\"name\":\"Desserts\",\"slug\":\"desserts\"}"))
                .build();
        category3 = menuContentRepository.save(category3);

        // Create products with relations to different categories
        var product1Body = Map.of(
                "collection", "product",
                "content", Map.of("name", "Double Espresso Collection Test", "price", 47),
                "relations", Map.of("category", List.of(category1.getId().toString()))
        );

        mvc.perform(post("/api/v1/menu/{menuId}/content", menu.getId())
                        .contentType(MediaType.APPLICATION_JSON.toString())
                        .content(mapper.writeValueAsBytes(product1Body))
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk());

        var product2Body = Map.of(
                "collection", "product",
                "content", Map.of("name", "Iced Latte Collection Test", "price", 57),
                "relations", Map.of("category", List.of(category2.getId().toString()))
        );

        mvc.perform(post("/api/v1/menu/{menuId}/content", menu.getId())
                        .contentType(MediaType.APPLICATION_JSON.toString())
                        .content(mapper.writeValueAsBytes(product2Body))
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk());

        var product3Body = Map.of(
                "collection", "product",
                "content", Map.of("name", "Tiramisu Collection Test", "price", 67),
                "relations", Map.of("category", List.of(category3.getId().toString()))
        );

        mvc.perform(post("/api/v1/menu/{menuId}/content", menu.getId())
                        .contentType(MediaType.APPLICATION_JSON.toString())
                        .content(mapper.writeValueAsBytes(product3Body))
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk());

        // GET the entire product collection
        mvc.perform(get("/api/v1/menu/{menuId}/content/{collection}", menu.getId(), "product")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data").isArray())
                // Verify we have at least our 3 products (there may be more from other tests)
                .andExpect(jsonPath("$.data[?(@.data.name == 'Double Espresso Collection Test')]").exists())
                .andExpect(jsonPath("$.data[?(@.data.name == 'Iced Latte Collection Test')]").exists())
                .andExpect(jsonPath("$.data[?(@.data.name == 'Tiramisu Collection Test')]").exists())
                // Verify first product with hydrated relation
                .andExpect(jsonPath("$.data[?(@.data.name == 'Double Espresso Collection Test')].data.price").value(47))
                .andExpect(jsonPath("$.data[?(@.data.name == 'Double Espresso Collection Test')].resolved.category[0].slug").value("hot-drinks"))
                // Verify second product with hydrated relation
                .andExpect(jsonPath("$.data[?(@.data.name == 'Iced Latte Collection Test')].data.price").value(57))
                .andExpect(jsonPath("$.data[?(@.data.name == 'Iced Latte Collection Test')].resolved.category[0].slug").value("cold-drinks"))
                // Verify third product with hydrated relation
                .andExpect(jsonPath("$.data[?(@.data.name == 'Tiramisu Collection Test')].data.price").value(67))
                .andExpect(jsonPath("$.data[?(@.data.name == 'Tiramisu Collection Test')].resolved.category[0].slug").value("desserts"));
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

    @Test
    public void delete_content_should_remove_item() throws Exception {
        setup();

        // Create a product
        var productBody = Map.of(
                "collection", "product",
                "content", Map.of("name", "Espresso", "price", 45)
        );

        var productRes = mvc.perform(post("/api/v1/menu/{menuId}/content", menu.getId())
                        .contentType(MediaType.APPLICATION_JSON.toString())
                        .content(mapper.writeValueAsBytes(productBody))
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andReturn();

        var productId = mapper.readTree(productRes.getResponse().getContentAsByteArray()).get("data").get("id").asText();

        // Verify item exists
        mvc.perform(get("/api/v1/menu/{menuId}/content/{collection}/{itemId}", menu.getId(), "product", productId)
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk());

        // Delete the item
        mvc.perform(delete("/api/v1/menu/{menuId}/content/{collection}/{itemId}", menu.getId(), "product", productId)
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.message", is("Menu content deleted successfully")));

        // Verify item is deleted
        mvc.perform(get("/api/v1/menu/{menuId}/content/{collection}/{itemId}", menu.getId(), "product", productId)
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isNotFound());
    }

    @Test
    public void delete_content_with_relations_should_cascade() throws Exception {
        setup();

        // Create a category
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

        // Create a product with relation to category
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

        // Verify product has relation
        mvc.perform(get("/api/v1/menu/{menuId}/content/{collection}/{itemId}", menu.getId(), "product", productId)
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.resolved.category[0].slug", is("coffee")));

        // Delete the product
        mvc.perform(delete("/api/v1/menu/{menuId}/content/{collection}/{itemId}", menu.getId(), "product", productId)
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk());

        // Verify product is deleted
        mvc.perform(get("/api/v1/menu/{menuId}/content/{collection}/{itemId}", menu.getId(), "product", productId)
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isNotFound());

        // Verify category still exists (it was a target, not source)
        mvc.perform(get("/api/v1/menu/{menuId}/content/{collection}/{itemId}", menu.getId(), "category", categoryId)
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk());
    }

    @Test
    public void delete_content_bulk_should_remove_multiple_items() throws Exception {
        setup();

        // Create multiple products
        var product1Body = Map.of(
                "collection", "product",
                "content", Map.of("name", "Coffee", "price", 40)
        );

        var product1Res = mvc.perform(post("/api/v1/menu/{menuId}/content", menu.getId())
                        .contentType(MediaType.APPLICATION_JSON.toString())
                        .content(mapper.writeValueAsBytes(product1Body))
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andReturn();

        var product1Id = mapper.readTree(product1Res.getResponse().getContentAsByteArray()).get("data").get("id").asText();

        var product2Body = Map.of(
                "collection", "product",
                "content", Map.of("name", "Tea", "price", 30)
        );

        var product2Res = mvc.perform(post("/api/v1/menu/{menuId}/content", menu.getId())
                        .contentType(MediaType.APPLICATION_JSON.toString())
                        .content(mapper.writeValueAsBytes(product2Body))
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andReturn();

        var product2Id = mapper.readTree(product2Res.getResponse().getContentAsByteArray()).get("data").get("id").asText();

        var product3Body = Map.of(
                "collection", "product",
                "content", Map.of("name", "Juice", "price", 50)
        );

        var product3Res = mvc.perform(post("/api/v1/menu/{menuId}/content", menu.getId())
                        .contentType(MediaType.APPLICATION_JSON.toString())
                        .content(mapper.writeValueAsBytes(product3Body))
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andReturn();

        var product3Id = mapper.readTree(product3Res.getResponse().getContentAsByteArray()).get("data").get("id").asText();

        // Bulk delete product1 and product2
        var deleteBody = Map.of(
                "item_ids", List.of(product1Id, product2Id)
        );

        mvc.perform(delete("/api/v1/menu/{menuId}/content/{collection}", menu.getId(), "product")
                        .contentType(MediaType.APPLICATION_JSON.toString())
                        .content(mapper.writeValueAsBytes(deleteBody))
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.message", is("2 menu content items deleted successfully")));

        // Verify deleted items are gone
        mvc.perform(get("/api/v1/menu/{menuId}/content/{collection}/{itemId}", menu.getId(), "product", product1Id)
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isNotFound());

        mvc.perform(get("/api/v1/menu/{menuId}/content/{collection}/{itemId}", menu.getId(), "product", product2Id)
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isNotFound());

        // Verify product3 still exists
        mvc.perform(get("/api/v1/menu/{menuId}/content/{collection}/{itemId}", menu.getId(), "product", product3Id)
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk());
    }

    @Test
    public void delete_content_bulk_with_relations_should_cascade() throws Exception {
        setup();

        // Create categories
        var category1Body = Map.of(
                "collection", "category",
                "content", Map.of("name", "Hot", "slug", "hot")
        );

        var category1Res = mvc.perform(post("/api/v1/menu/{menuId}/content", menu.getId())
                        .contentType(MediaType.APPLICATION_JSON.toString())
                        .content(mapper.writeValueAsBytes(category1Body))
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andReturn();

        var category1Id = mapper.readTree(category1Res.getResponse().getContentAsByteArray()).get("data").get("id").asText();

        var category2Body = Map.of(
                "collection", "category",
                "content", Map.of("name", "Cold", "slug", "cold")
        );

        var category2Res = mvc.perform(post("/api/v1/menu/{menuId}/content", menu.getId())
                        .contentType(MediaType.APPLICATION_JSON.toString())
                        .content(mapper.writeValueAsBytes(category2Body))
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andReturn();

        var category2Id = mapper.readTree(category2Res.getResponse().getContentAsByteArray()).get("data").get("id").asText();

        // Create products with relations
        var product1Body = Map.of(
                "collection", "product",
                "content", Map.of("name", "Cappuccino", "price", 55),
                "relations", Map.of("category", List.of(category1Id))
        );

        var product1Res = mvc.perform(post("/api/v1/menu/{menuId}/content", menu.getId())
                        .contentType(MediaType.APPLICATION_JSON.toString())
                        .content(mapper.writeValueAsBytes(product1Body))
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andReturn();

        var product1Id = mapper.readTree(product1Res.getResponse().getContentAsByteArray()).get("data").get("id").asText();

        var product2Body = Map.of(
                "collection", "product",
                "content", Map.of("name", "Iced Coffee", "price", 60),
                "relations", Map.of("category", List.of(category2Id))
        );

        var product2Res = mvc.perform(post("/api/v1/menu/{menuId}/content", menu.getId())
                        .contentType(MediaType.APPLICATION_JSON.toString())
                        .content(mapper.writeValueAsBytes(product2Body))
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andReturn();

        var product2Id = mapper.readTree(product2Res.getResponse().getContentAsByteArray()).get("data").get("id").asText();

        // Bulk delete both products
        var deleteBody = Map.of(
                "item_ids", List.of(product1Id, product2Id)
        );

        mvc.perform(delete("/api/v1/menu/{menuId}/content/{collection}", menu.getId(), "product")
                        .contentType(MediaType.APPLICATION_JSON.toString())
                        .content(mapper.writeValueAsBytes(deleteBody))
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk());

        // Verify products are deleted
        mvc.perform(get("/api/v1/menu/{menuId}/content/{collection}/{itemId}", menu.getId(), "product", product1Id)
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isNotFound());

        mvc.perform(get("/api/v1/menu/{menuId}/content/{collection}/{itemId}", menu.getId(), "product", product2Id)
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isNotFound());

        // Verify categories still exist (they were targets, not sources)
        mvc.perform(get("/api/v1/menu/{menuId}/content/{collection}/{itemId}", menu.getId(), "category", category1Id)
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk());

        mvc.perform(get("/api/v1/menu/{menuId}/content/{collection}/{itemId}", menu.getId(), "category", category2Id)
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk());
    }

    @Test
    public void delete_content_should_return_not_found_for_non_existent_item() throws Exception {
        setup();

        // Try to delete non-existent item
        String nonExistentId = java.util.UUID.randomUUID().toString();

        mvc.perform(delete("/api/v1/menu/{menuId}/content/{collection}/{itemId}", menu.getId(), "product", nonExistentId)
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isNotFound());
    }

    @Test
    public void delete_content_bulk_should_return_not_found_for_non_existent_items() throws Exception {
        setup();

        // Create one product
        var productBody = Map.of(
                "collection", "product",
                "content", Map.of("name", "Coffee", "price", 40)
        );

        var productRes = mvc.perform(post("/api/v1/menu/{menuId}/content", menu.getId())
                        .contentType(MediaType.APPLICATION_JSON.toString())
                        .content(mapper.writeValueAsBytes(productBody))
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andReturn();

        var productId = mapper.readTree(productRes.getResponse().getContentAsByteArray()).get("data").get("id").asText();

        // Try to bulk delete with one valid and one non-existent item
        String nonExistentId = java.util.UUID.randomUUID().toString();
        var deleteBody = Map.of(
                "item_ids", List.of(productId, nonExistentId)
        );

        mvc.perform(delete("/api/v1/menu/{menuId}/content/{collection}", menu.getId(), "product")
                        .contentType(MediaType.APPLICATION_JSON.toString())
                        .content(mapper.writeValueAsBytes(deleteBody))
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
