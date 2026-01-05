package com.furkancavdar.qrmenu.menu_module.content;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.furkancavdar.qrmenu.auth.application.port.out.UserRepositoryPort;
import com.furkancavdar.qrmenu.auth.domain.User;
import com.furkancavdar.qrmenu.menu_module.application.port.out.MenuContentRepositoryPort;
import com.furkancavdar.qrmenu.menu_module.application.port.out.MenuRepositoryPort;
import com.furkancavdar.qrmenu.menu_module.domain.Menu;
import com.furkancavdar.qrmenu.menu_module.domain.MenuContentItem;
import com.furkancavdar.qrmenu.menu_module.domain.MenuContentRelation;
import com.furkancavdar.qrmenu.theme_module.application.port.out.ThemeRepositoryPort;
import com.furkancavdar.qrmenu.theme_module.domain.Theme;
import com.furkancavdar.qrmenu.theme_module.domain.ThemeCategory;
import com.furkancavdar.qrmenu.theme_module.domain.ThemeManifest;
import com.redis.testcontainers.RedisContainer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@SpringBootTest
@Transactional
public class MenuContentRepositoryTest {

  @Container @ServiceConnection
  static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

  @Container @ServiceConnection static RedisContainer redis = new RedisContainer("redis:6.2.6");

  @Autowired MenuContentRepositoryPort menuContentRepository;
  @Autowired private UserRepositoryPort userRepositoryPort;
  @Autowired private ThemeRepositoryPort themeRepositoryPort;
  @Autowired private MenuRepositoryPort menuRepositoryPort;
  @Autowired ObjectMapper mapper;

  /** Record to hold test fixture data for owner, theme, and menu. */
  private record TestFixture(User owner, Theme theme, Menu menu) {}

  /**
   * Creates and saves a complete test fixture with owner, theme, and menu.
   *
   * @param username unique username for the owner
   * @param email unique email for the owner
   * @return TestFixture containing the saved owner, theme, and menu
   */
  private TestFixture createTestFixture(String username, String email) {
    // Create an owner
    User owner = new User(username, "password", email);
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
    Map<String, JsonNode> uiSchemas = new HashMap<>();

    // Create a theme
    Theme theme =
        new Theme(
            owner,
            "thumbnail_url",
            "location_url",
            true,
            ThemeCategory.OTHER,
            themeManifest,
            themeSchemas,
            uiSchemas);
    theme = themeRepositoryPort.save(theme);

    // Create a menu
    Menu menu = new Menu("test menu", owner, theme);
    menu = menuRepositoryPort.save(menu);

    return new TestFixture(owner, theme, menu);
  }

  private JsonNode obj(String json) throws Exception {
    return mapper.readTree(json);
  }

  @Test
  public void saveAndReadRelationsInOrder() throws Exception {
    TestFixture fixture = createTestFixture("username", "email");

    // Create a category
    MenuContentItem category =
        MenuContentItem.builder()
            .menu(fixture.menu())
            .ownerId(fixture.owner().getId())
            .theme(fixture.theme())
            .collectionName("category")
            .data(obj("{\"name\":\"Coffee\",\"slug\":\"coffee\"}"))
            .build();
    category = menuContentRepository.save(category);

    // Create a products
    MenuContentItem product =
        MenuContentItem.builder()
            .menu(fixture.menu())
            .ownerId(fixture.owner().getId())
            .theme(fixture.theme())
            .collectionName("product")
            .data(obj("{\"name\":\"Espresso\",\"price\":45}"))
            .build();
    product = menuContentRepository.save(product);

    // Two relations
    MenuContentItem upsell1 =
        MenuContentItem.builder()
            .menu(fixture.menu())
            .ownerId(fixture.owner().getId())
            .theme(fixture.theme())
            .collectionName("product")
            .data(obj("{\"name\":\"Double Espresso\",\"price\":45}"))
            .build();
    MenuContentItem upsell2 =
        MenuContentItem.builder()
            .menu(fixture.menu())
            .ownerId(fixture.owner().getId())
            .theme(fixture.theme())
            .collectionName("product")
            .data(obj("{\"name\":\"Cappuccino\",\"price\":40}"))
            .build();
    upsell1 = menuContentRepository.save(upsell1);
    upsell2 = menuContentRepository.save(upsell2);

    // Write relations
    menuContentRepository.save(newRel(product, "category", category, null));
    menuContentRepository.save(newRel(product, "upsell_items", upsell1, 0));
    menuContentRepository.save(newRel(product, "upsell_items", upsell2, 1));

    List<MenuContentRelation> upsells =
        menuContentRepository.findBySourceItemIdAndFieldNameOrderByPositionAsc(
            product.getId(), "upsell_items");
    assertThat(upsells).hasSize(2);
    assertThat(upsells.get(0).getTargetItem().getId()).isEqualTo(upsell1.getId());
    assertThat(upsells.get(1).getTargetItem().getId()).isEqualTo(upsell2.getId());
  }

  @Test
  public void uniquePositionWithinList_enforced() {
    TestFixture fixture = createTestFixture("username2", "email2");

    MenuContentItem src =
        menuContentRepository.save(dummyItem(fixture.menu(), fixture.owner(), fixture.theme()));
    MenuContentItem t1 =
        menuContentRepository.save(dummyItem(fixture.menu(), fixture.owner(), fixture.theme()));
    MenuContentItem t2 =
        menuContentRepository.save(dummyItem(fixture.menu(), fixture.owner(), fixture.theme()));

    menuContentRepository.save(newRel(src, "addons", t1, 0));
    // The second save with duplicate position should fail
    MenuContentRelation duplicateRel = newRel(src, "addons", t2, 0);
    assertThatThrownBy(
            () -> {
              menuContentRepository.save(duplicateRel);
            })
        .isInstanceOf(DataIntegrityViolationException.class);
  }

  @Test
  public void cascadeDelete_removesRelationsWhenSourceDeleted() {
    TestFixture fixture = createTestFixture("username3", "email3");

    MenuContentItem src =
        menuContentRepository.save(dummyItem(fixture.menu(), fixture.owner(), fixture.theme()));
    MenuContentItem tgt =
        menuContentRepository.save(dummyItem(fixture.menu(), fixture.owner(), fixture.theme()));
    menuContentRepository.save(newRel(src, "category", tgt, null));
    assertThat(menuContentRepository.findBySourceItemId(src.getId())).hasSize(1);

    // Delete source item - should cascade delete the relation
    menuContentRepository.delete(src);
    assertThat(menuContentRepository.findBySourceItemId(src.getId())).isEmpty();
    // Target should still exist
    assertThat(menuContentRepository.findById(tgt.getId())).isPresent();
  }

  @Test
  public void canCheckIfItemIsReferencedAsTarget() {
    TestFixture fixture = createTestFixture("restrictuser", "restrict@example.com");

    MenuContentItem src =
        menuContentRepository.save(dummyItem(fixture.menu(), fixture.owner(), fixture.theme()));
    MenuContentItem tgt =
        menuContentRepository.save(dummyItem(fixture.menu(), fixture.owner(), fixture.theme()));
    MenuContentItem notReferenced =
        menuContentRepository.save(dummyItem(fixture.menu(), fixture.owner(), fixture.theme()));

    // Create relation: src -> tgt
    menuContentRepository.save(newRel(src, "category", tgt, null));

    // Verify we can check if items are referenced as targets
    assertThat(menuContentRepository.existsByTargetItemId(tgt.getId())).isTrue();
    assertThat(menuContentRepository.existsByTargetItemId(notReferenced.getId())).isFalse();
    assertThat(menuContentRepository.findByTargetItemId(tgt.getId())).hasSize(1);
    assertThat(menuContentRepository.findByTargetItemId(notReferenced.getId())).isEmpty();
  }

  private MenuContentItem dummyItem(Menu menu, User owner, Theme theme) {
    MenuContentItem it =
        MenuContentItem.builder()
            .menu(menu)
            .ownerId(owner.getId())
            .theme(theme)
            .collectionName("x")
            .data(mapper.createObjectNode().put("n", "x"))
            .build();
    return it;
  }

  private MenuContentRelation newRel(
      MenuContentItem src, String field, MenuContentItem tgt, Integer pos) {
    MenuContentRelation rel =
        MenuContentRelation.builder()
            .sourceItem(src)
            .fieldName(field)
            .targetItem(tgt)
            .position(pos)
            .build();
    return rel;
  }

  @DynamicPropertySource
  private static void registerRedisProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.data.redis.host", redis::getHost);
    registry.add("spring.data.redis.port", () -> redis.getFirstMappedPort());
  }
}
