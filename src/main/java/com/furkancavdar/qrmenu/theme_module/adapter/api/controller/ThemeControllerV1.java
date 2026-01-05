package com.furkancavdar.qrmenu.theme_module.adapter.api.controller;

import com.furkancavdar.qrmenu.auth.application.port.out.UserRepositoryPort;
import com.furkancavdar.qrmenu.auth.domain.User;
import com.furkancavdar.qrmenu.common.ApiResponse;
import com.furkancavdar.qrmenu.common.exception.ResourceNotFoundException;
import com.furkancavdar.qrmenu.theme_module.adapter.api.dto.mapper.RegisterThemeRequestMapper;
import com.furkancavdar.qrmenu.theme_module.adapter.api.dto.mapper.ThemeManifestResponseMapper;
import com.furkancavdar.qrmenu.theme_module.adapter.api.dto.mapper.ThemeResponseMapper;
import com.furkancavdar.qrmenu.theme_module.adapter.api.dto.mapper.ThemeSchemasResponseMapper;
import com.furkancavdar.qrmenu.theme_module.adapter.api.dto.payload.request.RegisterThemeRequestDto;
import com.furkancavdar.qrmenu.theme_module.adapter.api.dto.payload.request.UnregisterThemeRequestDto;
import com.furkancavdar.qrmenu.theme_module.adapter.api.dto.payload.response.ThemeManifestResponseDto;
import com.furkancavdar.qrmenu.theme_module.adapter.api.dto.payload.response.ThemeResponseDto;
import com.furkancavdar.qrmenu.theme_module.adapter.api.dto.payload.response.ThemeSchemasResponseDto;
import com.furkancavdar.qrmenu.theme_module.application.port.in.ThemeRegisterUseCase;
import com.furkancavdar.qrmenu.theme_module.application.port.in.dto.ThemeManifestResultDto;
import com.furkancavdar.qrmenu.theme_module.application.port.in.dto.ThemeSchemasResultDto;
import jakarta.validation.Valid;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
@RequestMapping("/api/v1/theme")
@RequiredArgsConstructor
public class ThemeControllerV1 {

  private final ThemeRegisterUseCase themeRegisterUseCase;
  private final UserRepositoryPort userRepository;

  @GetMapping("/{themeId}")
  public ResponseEntity<ApiResponse<ThemeManifestResponseDto>> getThemeManifest(
      @PathVariable String themeId) {
    try {
      ThemeManifestResultDto manifestResult =
          themeRegisterUseCase.getManifest(Long.valueOf(themeId));
      ThemeManifestResponseDto response =
          ThemeManifestResponseMapper.fromThemeManifestResultDto(manifestResult);
      return ResponseEntity.ok(ApiResponse.success(response));
    } catch (ResourceNotFoundException e) {
      log.error("ThemeControllerV1:getThemeManifest error: {}", e.getMessage());
      return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error(e.getMessage()));
    }
  }

  @GetMapping
  public ResponseEntity<ApiResponse<Page<ThemeResponseDto>>> getAllThemes(
      @RequestParam(defaultValue = "0") Integer page,
      @RequestParam(defaultValue = "20") Integer size) {
    Page<ThemeResponseDto> allThemes =
        themeRegisterUseCase.getAllThemes(page, size).map(ThemeResponseMapper::fromThemeDto);
    return ResponseEntity.ok(ApiResponse.success(allThemes));
  }

  @GetMapping("/{themeId}/schemas")
  public ResponseEntity<ApiResponse<ThemeSchemasResponseDto>> getThemeSchemas(
      @PathVariable String themeId,
      @RequestParam(required = false) List<String> refs,
      @RequestParam(value = "uiSchema", required = false, defaultValue = "false")
          boolean includeUiSchemaFlag) {
    try {
      ThemeSchemasResultDto schemasResult =
          themeRegisterUseCase.getSchemas(Long.valueOf(themeId), refs, includeUiSchemaFlag);
      ThemeSchemasResponseDto response =
          ThemeSchemasResponseMapper.fromThemeSchemasResultDto(schemasResult);
      return ResponseEntity.ok(ApiResponse.success(response));
    } catch (ResourceNotFoundException e) {
      log.error("ThemeControllerV1:getThemeSchemas error: {}", e.getMessage());
      return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error(e.getMessage()));
    }
  }

  @GetMapping("/test")
  public ResponseEntity<ApiResponse<UserDetails>> test(
      @AuthenticationPrincipal UserDetails userDetails) {
    log.info("test userdetails: {}", userDetails);
    return ResponseEntity.ok(ApiResponse.success(userDetails));
  }

  @PostMapping("/register")
  @PreAuthorize("hasRole('DEVELOPER') or hasRole('ADMIN')")
  public ResponseEntity<ApiResponse<String>> registerTheme(
      @RequestPart("file") MultipartFile zipFile,
      @RequestPart(value = "image", required = false) MultipartFile imageFile,
      @Valid @RequestPart("data") RegisterThemeRequestDto registerThemeRequestDto,
      @AuthenticationPrincipal UserDetails userDetails) {
    log.info("userdetails: {}", userDetails);
    log.info("json data: {}", registerThemeRequestDto);
    ValidateResponse validateZipUpload = validateZipUpload(zipFile);
    if (!validateZipUpload.isValid()) {
      return ResponseEntity.badRequest().body(ApiResponse.error(validateZipUpload.message()));
    }

    if (imageFile != null && !imageFile.isEmpty()) {
      ValidateResponse validateImageUpload = validateImageUpload(imageFile);
      log.info(validateImageUpload.toString());
      if (!validateImageUpload.isValid()) {
        return ResponseEntity.badRequest().body(ApiResponse.error(validateImageUpload.message()));
      }
    }

    try {
      Optional<User> optionalUser = userRepository.findByUsername(userDetails.getUsername());
      if (optionalUser.isEmpty()) {
        return ResponseEntity.internalServerError().body(ApiResponse.error("User not found"));
      }

      log.info("ThemeControllerV1:registerTheme user: {}", optionalUser.get());

      themeRegisterUseCase.registerTheme(
          zipFile.getInputStream(),
          imageFile != null ? imageFile.getInputStream() : null,
          RegisterThemeRequestMapper.toThemeDto(
              null, registerThemeRequestDto, optionalUser.get(), null, null, null));
    } catch (Exception e) {
      log.error("ThemeControllerV1:registerTheme error {}", e.getMessage());
      return ResponseEntity.internalServerError().body(ApiResponse.error(e.getMessage()));
    }

    return ResponseEntity.ok(ApiResponse.success("Theme registered successfully"));
  }

  @PostMapping("/unregister")
  @PreAuthorize("hasRole('DEVELOPER') or hasRole('ADMIN')")
  public ResponseEntity<ApiResponse<String>> unregisterTheme(
      @Valid @RequestBody UnregisterThemeRequestDto unregisterThemeRequestDto,
      @AuthenticationPrincipal UserDetails userDetails) {
    try {
      Boolean isDeveloper =
          userDetails.getAuthorities().stream()
              .anyMatch(authority -> authority.getAuthority().equals("ROLE_DEVELOPER"));
      Boolean isAdmin =
          userDetails.getAuthorities().stream()
              .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"));
      themeRegisterUseCase.unregisterTheme(
          unregisterThemeRequestDto.getThemeId(), userDetails.getUsername(), isAdmin, isDeveloper);
    } catch (Exception e) {
      log.error("ThemeControllerV1:unregister error {}", e.getMessage());
      return ResponseEntity.internalServerError().body(ApiResponse.error(e.getMessage()));
    }
    return ResponseEntity.ok(ApiResponse.success("Theme unregistered successfully"));
  }

  private ValidateResponse validateZipUpload(MultipartFile zipFile) {
    if (zipFile == null || zipFile.isEmpty()) {
      return new ValidateResponse(Boolean.FALSE, "Zip File is empty");
    }
    if (zipFile.getOriginalFilename() != null
        && !zipFile.getOriginalFilename().toLowerCase().endsWith(".zip")) {
      return new ValidateResponse(Boolean.FALSE, "Only ZIP files are supported");
    }

    return new ValidateResponse(Boolean.TRUE, "");
  }

  private ValidateResponse validateImageUpload(MultipartFile imageFile) {
    if (imageFile.getOriginalFilename() != null
        && Arrays.stream(ImageSuffix.values())
            .map(ImageSuffix::getSuffix)
            .map(String::toLowerCase)
            .noneMatch(imageFile.getOriginalFilename().toLowerCase()::endsWith)) {
      String suffixes =
          Arrays.stream(ImageSuffix.values())
              .map(ImageSuffix::getSuffix)
              .collect(Collectors.joining(", "));
      return new ValidateResponse(Boolean.FALSE, "Only %s files are supported".formatted(suffixes));
    }
    return new ValidateResponse(Boolean.TRUE, "");
  }

  private record ValidateResponse(Boolean isValid, String message) {}

  private enum ImageSuffix {
    JPG(".JPG"),
    JPEG(".JPEG"),
    PNG(".PNG"),
    WEBP(".WEBP");

    private final String suffix;

    ImageSuffix(String suffix) {
      this.suffix = suffix;
    }

    String getSuffix() {
      return suffix;
    }
  }
}
