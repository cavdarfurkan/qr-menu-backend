package com.furkancavdar.qrmenu.menu_module.adapter.api.controller;

import com.furkancavdar.qrmenu.common.ApiResponse;
import com.furkancavdar.qrmenu.menu_module.adapter.api.dto.mapper.BuildMenuResponseMapper;
import com.furkancavdar.qrmenu.menu_module.adapter.api.dto.mapper.CreateMenuRequestMapper;
import com.furkancavdar.qrmenu.menu_module.adapter.api.dto.mapper.UpdateMenuRequestMapper;
import com.furkancavdar.qrmenu.menu_module.adapter.api.dto.payload.request.BuildMenuRequestDto;
import com.furkancavdar.qrmenu.menu_module.adapter.api.dto.payload.request.CreateMenuRequestDto;
import com.furkancavdar.qrmenu.menu_module.adapter.api.dto.payload.request.UpdateMenuRequestDto;
import com.furkancavdar.qrmenu.menu_module.adapter.api.dto.payload.response.BuildMenuResponseDto;
import com.furkancavdar.qrmenu.menu_module.adapter.api.dto.payload.response.DomainAvailabilityResponseDto;
import com.furkancavdar.qrmenu.menu_module.application.port.in.MenuUseCase;
import com.furkancavdar.qrmenu.menu_module.application.port.in.dto.MenuDto;
import com.furkancavdar.qrmenu.menu_module.application.port.in.dto.UserMenuDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/menu")
@RequiredArgsConstructor
public class MenuControllerV1 {

  private final MenuUseCase menuUseCase;

  @PostMapping("/create")
  public ResponseEntity<ApiResponse<String>> createMenu(
      @Valid @RequestBody CreateMenuRequestDto createMenuRequestDto,
      @AuthenticationPrincipal UserDetails userDetails) {
    log.info("MenuControllerV1:createMenu");
    MenuDto menuDto =
        CreateMenuRequestMapper.toMenuDto(createMenuRequestDto, userDetails.getUsername());
    try {
      menuUseCase.createMenu(menuDto);
    } catch (Exception e) {
      log.error("MenuControllerV1:createMenu error {}", e.getMessage());
      return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
    }

    return ResponseEntity.status(HttpStatus.CREATED)
        .body(ApiResponse.success("Menu created successfully"));
  }

  @DeleteMapping("/delete/{menuId}")
  public ResponseEntity<ApiResponse<?>> deleteMenu(
      @Valid @PathVariable @NotNull @Positive Long menuId,
      @AuthenticationPrincipal UserDetails userDetails) {
    log.info("MenuControllerV1:deleteMenu");
    try {
      Boolean isAdmin =
          userDetails.getAuthorities().stream()
              .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"));
      menuUseCase.deleteMenu(menuId, userDetails.getUsername(), isAdmin);
    } catch (Exception e) {
      log.error("MenuControllerV1:deleteMenu error {}", e.getMessage());
      return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
    }
    return ResponseEntity.ok(ApiResponse.success("Menu deleted successfully"));
  }

  @PostMapping("/build")
  public ResponseEntity<ApiResponse<BuildMenuResponseDto>> buildMenu(
      @RequestBody BuildMenuRequestDto buildMenuRequestDto,
      @AuthenticationPrincipal UserDetails userDetails) {
    Long menuId = buildMenuRequestDto.getMenuId();
    String ownerName = userDetails.getUsername();

    try {
      BuildMenuResponseDto response =
          BuildMenuResponseMapper.fromBuildMenuResultDto(menuUseCase.buildMenu(menuId, ownerName));

      return ResponseEntity.accepted()
          .header(HttpHeaders.LOCATION, response.getStatusUrl())
          .body(
              ApiResponse.success("Menu build request has been accepted for processing", response));
    } catch (Exception e) {
      log.error("MenuControllerV1:buildMenu error {}", e.getMessage());
      return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
    }
  }

  @GetMapping("/all")
  public ResponseEntity<ApiResponse<List<UserMenuDto>>> getAllUserMenus(
      @AuthenticationPrincipal UserDetails userDetails) {
    String ownerName = userDetails.getUsername();
    List<UserMenuDto> menus = menuUseCase.allUserMenus(ownerName);
    return ResponseEntity.ok(ApiResponse.success(menus));
  }

  @GetMapping("/{menuId}")
  public ResponseEntity<ApiResponse<MenuDto>> getMenuById(
      @AuthenticationPrincipal UserDetails userDetails, @PathVariable Long menuId) {
    String ownerName = userDetails.getUsername();
    MenuDto menu = menuUseCase.getMenu(menuId, ownerName);
    return ResponseEntity.ok(ApiResponse.success(menu));
  }

  @PutMapping("/{menuId}")
  public ResponseEntity<ApiResponse<String>> updateMenu(
      @Valid @PathVariable @NotNull @Positive Long menuId,
      @Valid @RequestBody UpdateMenuRequestDto updateMenuRequestDto,
      @AuthenticationPrincipal UserDetails userDetails) {
    log.info("MenuControllerV1:updateMenu");
    MenuDto menuDto = UpdateMenuRequestMapper.toMenuDto(updateMenuRequestDto);
    menuUseCase.updateMenu(menuId, menuDto, userDetails.getUsername());
    return ResponseEntity.ok(ApiResponse.success("Menu updated successfully"));
  }

  @GetMapping("/domain/available")
  public ResponseEntity<ApiResponse<DomainAvailabilityResponseDto>> checkDomainAvailability(
      @Valid @RequestParam @NotBlank String domain) {
    log.info("MenuControllerV1:checkDomainAvailability");
    boolean available = menuUseCase.checkDomainAvailability(domain);
    DomainAvailabilityResponseDto response = new DomainAvailabilityResponseDto(available);
    return ResponseEntity.ok(ApiResponse.success(response));
  }

  @PostMapping("/{menuId}/unpublish")
  public ResponseEntity<ApiResponse<BuildMenuResponseDto>> unpublishMenu(
      @Valid @PathVariable @NotNull @Positive Long menuId,
      @AuthenticationPrincipal UserDetails userDetails) {
    log.info("MenuControllerV1:unpublishMenu");
    BuildMenuResponseDto response =
        BuildMenuResponseMapper.fromBuildMenuResultDto(
            menuUseCase.unpublishMenu(menuId, userDetails.getUsername()));
    return ResponseEntity.accepted()
        .header(HttpHeaders.LOCATION, response.getStatusUrl())
        .body(
            ApiResponse.success(
                "Menu unpublish request has been accepted for processing", response));
  }
}
