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
import com.furkancavdar.qrmenu.theme_module.application.port.out.ThemeRepositoryPort;
import com.furkancavdar.qrmenu.theme_module.domain.Theme;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.NotImplementedException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class MenuService implements MenuUseCase {

  @Value("${app.base-url}")
  private String baseUrl;

  private final MenuRepositoryPort menuRepository;
  private final UserRepositoryPort userRepository;
  private final ThemeRepositoryPort themeRepository;

  private final MenuJobUseCase menuJobUseCase;
  private final MenuContentUseCase menuContentUseCase;

  private final RedisTemplate<String, BuildMenuJobDto> redisTemplate;

  private static final String BUILD_QUEUE = "queue:build:main";

  @Override
  public void createMenu(MenuDto menuDto) {
    User owner =
        userRepository
            .findByUsername(menuDto.getOwnerUsername())
            .orElseThrow(() -> new RuntimeException("User not found"));

    Theme selectedTheme =
        themeRepository
            .findById(menuDto.getSelectedThemeId())
            .orElseThrow(() -> new RuntimeException("Theme not found"));

    menuRepository.save(MenuDtoMapper.toMenu(menuDto, owner, selectedTheme));
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
    String siteName = DnsNameFormatter.toDnsLabel("%s-%s".formatted(menu.getMenuName(), ownerName));

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

    return MenuDtoMapper.toMenuDto(menu);
  }
}
