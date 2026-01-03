package com.furkancavdar.qrmenu.menu_module.application.service;

import com.furkancavdar.qrmenu.auth.application.port.out.UserRepositoryPort;
import com.furkancavdar.qrmenu.auth.domain.User;
import com.furkancavdar.qrmenu.common.exception.ResourceNotFoundException;
import com.furkancavdar.qrmenu.menu_module.adapter.api.dto.payload.queue.BuildMenuJobDto;
import com.furkancavdar.qrmenu.menu_module.application.port.in.MenuContentUseCase;
import com.furkancavdar.qrmenu.menu_module.application.port.in.MenuJobUseCase;
import com.furkancavdar.qrmenu.menu_module.application.port.in.MenuUseCase;
import com.furkancavdar.qrmenu.menu_module.application.port.in.dto.BuildMenuResultDto;
import com.furkancavdar.qrmenu.menu_module.application.port.in.dto.HydratedItemDto;
import com.furkancavdar.qrmenu.menu_module.application.port.in.dto.MenuDto;
import com.furkancavdar.qrmenu.menu_module.application.port.in.dto.UserMenuDto;
import com.furkancavdar.qrmenu.menu_module.application.port.in.mapper.MenuDtoMapper;
import com.furkancavdar.qrmenu.menu_module.application.port.in.mapper.UserMenuDtoMapper;
import com.furkancavdar.qrmenu.menu_module.application.port.out.MenuRepositoryPort;
import com.furkancavdar.qrmenu.menu_module.domain.Menu;
import com.furkancavdar.qrmenu.menu_module.domain.MenuJob;
import com.furkancavdar.qrmenu.menu_module.domain.MenuJobStatus;
import com.furkancavdar.qrmenu.menu_module.util.DnsNameFormatter;
import com.furkancavdar.qrmenu.menu_module.util.DomainUtility;
import com.furkancavdar.qrmenu.theme_module.application.port.out.ThemeRepositoryPort;
import com.furkancavdar.qrmenu.theme_module.domain.Theme;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.NotImplementedException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class MenuService implements MenuUseCase {

  @Value("${app.base-url}")
  private String baseUrl;

  @Value("${app.menu.base-domain}")
  private String baseDomain;

  private final MenuRepositoryPort menuRepository;
  private final UserRepositoryPort userRepository;
  private final ThemeRepositoryPort themeRepository;

  private final MenuJobUseCase menuJobUseCase;
  private final MenuContentUseCase menuContentUseCase;

  private final RedisTemplate<String, BuildMenuJobDto> redisTemplate;

  private static final String BUILD_QUEUE = "queue:build:main";

  @Override
  @Transactional
  public void createMenu(MenuDto menuDto) {
    User owner =
        userRepository
            .findByUsername(menuDto.getOwnerUsername())
            .orElseThrow(() -> new RuntimeException("User not found"));

    Theme selectedTheme =
        themeRepository
            .findById(menuDto.getSelectedThemeId())
            .orElseThrow(() -> new RuntimeException("Theme not found"));

    String customDomain = menuDto.getCustomDomain();
    if (customDomain == null || customDomain.trim().isEmpty()) {
      // Auto-generate subdomain
      String subdomain =
          DnsNameFormatter.toDnsLabel(
              "%s-%s".formatted(menuDto.getMenuName(), menuDto.getOwnerUsername()));
      customDomain = generateUniqueSubdomain(subdomain);
    } else {
      // Validate and normalize provided domain to extract subdomain
      try {
        customDomain = DomainUtility.normalizeDomainInput(customDomain, baseDomain);
      } catch (IllegalArgumentException e) {
        throw new IllegalArgumentException("Invalid domain format: " + e.getMessage());
      }

      // Check uniqueness (using subdomain)
      if (menuRepository.findByCustomDomain(customDomain).isPresent()) {
        String fullDomain = DomainUtility.combineSubdomainWithBase(customDomain, baseDomain);
        throw new IllegalArgumentException("Custom domain '" + fullDomain + "' is already taken");
      }
    }

    Menu menuToCreate = MenuDtoMapper.toMenu(menuDto, owner, selectedTheme);
    menuRepository.save(menuToCreate);
  }

  private String generateUniqueSubdomain(String baseSubdomain) {
    String candidateSubdomain = baseSubdomain;
    int suffix = 0;

    while (menuRepository.findByCustomDomain(candidateSubdomain).isPresent()) {
      suffix++;
      String suffixStr = "-" + suffix;
      // Ensure total length doesn't exceed DNS label limit (63 chars)
      int maxBaseLength = 63 - suffixStr.length();
      if (baseSubdomain.length() > maxBaseLength) {
        baseSubdomain = baseSubdomain.substring(0, maxBaseLength);
      }
      candidateSubdomain = baseSubdomain + suffixStr;
    }

    return candidateSubdomain;
  }

  @Override
  public void deleteMenu(Long menuId, String ownerName, Boolean isAdmin) {
    Menu menu =
        menuRepository.findById(menuId).orElseThrow(() -> new RuntimeException("Menu not found"));
    if (!isAdmin && !menu.isOwner(ownerName)) {
      throw new RuntimeException(ownerName + " is not the owner of menu " + menu.getMenuName());
    }

    menuRepository.delete(menu);
  }

  @Override
  public BuildMenuResultDto buildMenu(Long menuId, String ownerName) {
    Menu menu =
        menuRepository.findById(menuId).orElseThrow(() -> new RuntimeException("Menu not found"));
    if (!menu.isOwner(ownerName)) {
      throw new RuntimeException(ownerName + " is not the owner of menu " + menu.getMenuName());
    }

    Map<String, List<HydratedItemDto>> menuContents =
        menu.getSelectedTheme().getThemeManifest().getContentTypes().stream()
            .map((node) -> node.get("name").asText())
            .collect(
                Collectors.toMap(
                    collection -> collection,
                    collection ->
                        menuContentUseCase.getCollectionContent(ownerName, menuId, collection)));

    String jobId = UUID.randomUUID().toString();
    String statusUrl = baseUrl + "/api/v1/menu/job/" + jobId;
    String siteName;
    if (menu.getCustomDomain() != null && !menu.getCustomDomain().isEmpty()) {
      // Combine stored subdomain with base domain
      siteName = DomainUtility.combineSubdomainWithBase(menu.getCustomDomain(), baseDomain);
    } else {
      siteName = DnsNameFormatter.toDnsLabel("%s-%s".formatted(menu.getMenuName(), ownerName));
    }

    BuildMenuJobDto job =
        BuildMenuJobDto.builder()
            .themeLocationUrl(menu.getSelectedTheme().getThemeLocationUrl())
            .siteName(siteName)
            .contents(menuContents)
            .timestamp(System.currentTimeMillis())
            .statusUrl(statusUrl)
            .build();

    menuJobUseCase.save(new MenuJob(jobId, MenuJobStatus.PENDING));

    // Enqueue
    redisTemplate.opsForList().leftPush(BUILD_QUEUE, job);

    return new BuildMenuResultDto(statusUrl);
  }

  @Override
  public void publishMenu() {
    throw new NotImplementedException();
  }

  @Override
  public void unpublishMenu() {
    throw new NotImplementedException();
  }

  @Override
  public List<UserMenuDto> allUserMenus(String ownerName) {
    User owner =
        userRepository
            .findByUsername(ownerName)
            .orElseThrow(() -> new RuntimeException("User not found"));

    return menuRepository.findAllByOwnerId(owner.getId()).stream()
        .map(UserMenuDtoMapper::toUserMenuDto)
        .toList();
  }

  @Override
  public MenuDto getMenu(Long menuId, String ownerName) {
    Menu menu =
        menuRepository
            .findById(menuId)
            .orElseThrow(() -> new ResourceNotFoundException("Menu not found"));
    if (!menu.isOwner(ownerName)) {
      throw new RuntimeException(ownerName + " is not the owner of menu " + menu.getMenuName());
    }

    return MenuDtoMapper.toMenuDto(menu, baseDomain);
  }

  @Override
  @Transactional
  public void updateMenu(Long menuId, MenuDto menuDto, String ownerName) {
    Menu menu =
        menuRepository
            .findById(menuId)
            .orElseThrow(() -> new ResourceNotFoundException("Menu not found"));

    if (!menu.isOwner(ownerName)) {
      throw new AccessDeniedException(
          ownerName + " is not the owner of menu " + menu.getMenuName());
    }

    // Validate theme if provided
    Theme selectedTheme = menu.getSelectedTheme();
    if (menuDto.getSelectedThemeId() != null) {
      selectedTheme =
          themeRepository
              .findById(menuDto.getSelectedThemeId())
              .orElseThrow(() -> new ResourceNotFoundException("Theme not found"));

      // Check if theme is changing
      if (!selectedTheme.getId().equals(menu.getSelectedTheme().getId())) {
        // Theme is changing, delete all content
        log.info(
            "Theme changed from {} to {} for menu {}, deleting all content",
            menu.getSelectedTheme().getId(),
            selectedTheme.getId(),
            menuId);
        menuContentUseCase.deleteAllContentByMenuId(ownerName, menuId);
      }
    }

    // Validate and normalize custom domain if provided
    String customDomain = menu.getCustomDomain();
    if (menuDto.getCustomDomain() != null) {
      String providedDomain = menuDto.getCustomDomain().trim();
      if (providedDomain.isEmpty()) {
        // Empty string means clear the custom domain
        customDomain = null;
      } else {
        try {
          // Normalize input to extract subdomain
          customDomain = DomainUtility.normalizeDomainInput(providedDomain, baseDomain);
        } catch (IllegalArgumentException e) {
          throw new IllegalArgumentException("Invalid domain format: " + e.getMessage());
        }

        // Check uniqueness (excluding current menu) - using subdomain
        Optional<Menu> existingMenu = menuRepository.findByCustomDomain(customDomain);
        if (existingMenu.isPresent() && !existingMenu.get().getId().equals(menuId)) {
          String fullDomain = DomainUtility.combineSubdomainWithBase(customDomain, baseDomain);
          throw new IllegalArgumentException(
              "Custom domain '" + fullDomain + "' is already taken by another menu");
        }
      }
    }

    // Update menu fields
    String menuName = menuDto.getMenuName() != null ? menuDto.getMenuName() : menu.getMenuName();

    // Create updated menu
    Menu updatedMenu = new Menu(menuId, menuName, menu.getOwner(), selectedTheme, customDomain);
    menuRepository.save(updatedMenu);

    log.info("Menu {} updated successfully", menuId);
  }

  @Override
  public boolean checkDomainAvailability(String domain) {
    if (domain == null || domain.trim().isEmpty()) {
      throw new IllegalArgumentException("Domain cannot be null or empty");
    }

    try {
      // Normalize input to extract subdomain
      String normalizedSubdomain = DomainUtility.normalizeDomainInput(domain, baseDomain);
      return menuRepository.findByCustomDomain(normalizedSubdomain).isEmpty();
    } catch (IllegalArgumentException e) {
      throw new IllegalArgumentException("Invalid domain format: " + e.getMessage());
    }
  }
}
