package com.furkancavdar.qrmenu.theme_module.application.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.furkancavdar.qrmenu.common.exception.ResourceNotFoundException;
import com.furkancavdar.qrmenu.theme_module.application.port.in.ThemeRegisterUseCase;
import com.furkancavdar.qrmenu.theme_module.application.port.in.dto.ThemeDto;
import com.furkancavdar.qrmenu.theme_module.application.port.in.dto.ThemeManifestResultDto;
import com.furkancavdar.qrmenu.theme_module.application.port.in.dto.ThemeSchemasResultDto;
import com.furkancavdar.qrmenu.theme_module.application.port.in.mapper.ThemeDtoMapper;
import com.furkancavdar.qrmenu.theme_module.application.port.out.ThemeRepositoryPort;
import com.furkancavdar.qrmenu.theme_module.application.port.out.ThemeStoragePort;
import com.furkancavdar.qrmenu.theme_module.domain.Theme;
import com.furkancavdar.qrmenu.theme_module.domain.ThemeManifest;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ThemeRegisterService implements ThemeRegisterUseCase {

  @Value("${s3.placeholder-thumbnail}")
  private String PLACEHOLDER_THUMBNAIL;

  private final ThemeStoragePort themeStorage;
  private final ThemeRepositoryPort themeRepository;

  private final ObjectMapper objectMapper;

  @Override
  public void registerTheme(InputStream themeZipIs, InputStream thumbnailIs, ThemeDto themeDto) {
    // Buffer the input stream into memory to replay multiple times
    byte[] zipBytes;
    try {
      zipBytes = IOUtils.toByteArray(themeZipIs);
      ExtractResult extractResult = extractManifestAndSchemas(zipBytes);
      themeDto.setThemeManifest(extractResult.manifest);
      themeDto.setThemeSchemas(extractResult.themeSchemas);
      themeDto.setUiSchemas(extractResult.uiSchemas);
    } catch (Exception e) {
      log.error("ThemeRegisterService.registerTheme error: {}", e.getMessage());
      throw new RuntimeException(e.getMessage());
    }

    Optional<Theme> optionalTheme =
        themeRepository.findByThemeName(themeDto.getThemeManifest().getName());
    if (optionalTheme.isPresent()) {
      throw new RuntimeException("Theme already exists");
    }

    String location =
        themeStorage.putTheme(
            new ByteArrayInputStream(zipBytes), themeDto.getThemeManifest().getName());
    String thumbnailLocation = null;
    if (thumbnailIs != null) {
      thumbnailLocation =
          themeStorage.putThumbnail(thumbnailIs, themeDto.getThemeManifest().getName());
    }

    Theme theme =
        ThemeDtoMapper.toTheme(
            themeDto,
            thumbnailLocation != null ? thumbnailLocation : PLACEHOLDER_THUMBNAIL,
            location);
    themeRepository.save(theme);
    log.info("Theme registered: {}", themeDto);
  }

  @Override
  public void unregisterTheme(Long themeId, String ownerName, Boolean isAdmin) {
    Theme theme =
        themeRepository
            .findById(themeId)
            .orElseThrow(() -> new RuntimeException("Theme not found"));

    if (!isAdmin && !theme.isOwner(ownerName)) {
      throw new RuntimeException(
          ownerName + " is not the owner of theme " + theme.getThemeManifest().getName());
    }

    log.info("ThemeRegisterService:unregisterTheme theme: {}", theme.toString());

    themeRepository.deleteThemeById(theme.getId());
    themeStorage.deleteTheme(theme);

    log.info("Theme unregistered: {}", theme);
  }

  @Override
  public ThemeManifestResultDto getManifest(Long themeId) {
    Theme theme =
        themeRepository
            .findById(themeId)
            .orElseThrow(() -> new ResourceNotFoundException("Theme not found"));
    return new ThemeManifestResultDto(theme.getThemeManifest());
  }

  @Override
  public ThemeSchemasResultDto getSchemas(
      Long themeId, List<String> refs, boolean includeUiSchemaFlag) {
    Theme theme =
        themeRepository
            .findById(themeId)
            .orElseThrow(() -> new ResourceNotFoundException("Theme not found"));

    Map<String, JsonNode> themeSchemas = theme.getThemeSchemas();
    if (themeSchemas == null) {
      return new ThemeSchemasResultDto(0, null, null);
    }

    Map<String, JsonNode> uiSchemas = Collections.emptyMap();
    if (includeUiSchemaFlag) {
      uiSchemas = theme.getUiSchemas();
    }

    if (refs == null || refs.isEmpty()) {
      return new ThemeSchemasResultDto(themeSchemas.size(), themeSchemas, uiSchemas);
    }

    Map<String, JsonNode> filteredSchemas = new HashMap<>();
    themeSchemas.forEach(
        (k, v) -> {
          if (refs.contains(k)) {
            filteredSchemas.put(k, v);
          }
        });

    Map<String, JsonNode> filteredUiSchemas = new HashMap<>();
    if (uiSchemas != null) {
      uiSchemas.forEach(
          (k, v) -> {
            if (refs.contains(k)) {
              filteredUiSchemas.put(k, v);
            }
          });
    }

    return new ThemeSchemasResultDto(filteredSchemas.size(), filteredSchemas, filteredUiSchemas);
  }

  @Override
  public Page<ThemeDto> getAllThemes(Integer page, Integer size) {
    return themeRepository.getAllThemes(page, size).map(ThemeDtoMapper::toThemeDto);
  }

  private ExtractResult extractManifestAndSchemas(byte[] zipBytes) {
    final String manifestFilename = "manifest.json";
    final String themeSchemasDir = "schemas/";
    final String uiSchemasDir = "ui_schemas/";
    final String loaderLocationsFilename = ".loader_locations.json";

    if (!containsFiles(
        new ByteArrayInputStream(zipBytes),
        manifestFilename,
        themeSchemasDir,
        uiSchemasDir,
        loaderLocationsFilename)) {
      throw new RuntimeException(
          "Zip file does not contain required files: "
              + manifestFilename
              + " "
              + themeSchemasDir
              + " "
              + uiSchemasDir
              + " "
              + loaderLocationsFilename);
    }

    try {
      ThemeManifest manifest =
          extractFile(new ByteArrayInputStream(zipBytes), manifestFilename, ThemeManifest.class);

      Map<String, JsonNode> themeSchemas =
          manifest.getContentTypes().stream()
              .map(
                  node ->
                      Pair.of(
                          node,
                          extractFile(
                              new ByteArrayInputStream(zipBytes),
                              node.get("schemaPath").asText(),
                              JsonNode.class)))
              .collect(
                  Collectors.toMap(pair -> pair.getLeft().get("name").asText(), Pair::getRight));

      Map<String, JsonNode> uiSchemas =
          manifest.getContentTypes().stream()
              .map(
                  node ->
                      Pair.of(
                          node,
                          extractFile(
                              new ByteArrayInputStream(zipBytes),
                              node.get("uiSchemaPath").asText(),
                              JsonNode.class)))
              .collect(
                  Collectors.toMap(pair -> pair.getLeft().get("name").asText(), Pair::getRight));

      return new ExtractResult(manifest, themeSchemas, uiSchemas);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private Boolean containsFiles(InputStream is, String firstFile, String... otherFiles) {
    Set<String> filesToFind = new HashSet<>();
    filesToFind.add(firstFile);
    filesToFind.addAll(Arrays.asList(otherFiles));

    Map<String, Boolean> filePresence = new HashMap<>();
    for (String file : filesToFind) {
      filePresence.put(file, false);
    }

    try (ZipInputStream zis = new ZipInputStream(is)) {
      ZipEntry entry;
      while ((entry = zis.getNextEntry()) != null) {
        String entryName = entry.getName();
        if (!filePresence.containsKey(entryName)) {
          continue;
        }

        filePresence.put(entryName, true);

        if (!filePresence.containsValue(false)) {
          break;
        }
      }
    } catch (Exception e) {
      log.error("ThemeControllerV1:containsFiles error {}", e.getMessage());
      return Boolean.FALSE;
    }

    return !filePresence.containsValue(false);
  }

  private <T> T extractFile(InputStream is, String filename, Class<T> valueType) {
    try (ZipInputStream zis = new ZipInputStream(is)) {
      byte[] buffer = new byte[1024]; // 1 KB
      int bytesRead;

      ZipEntry entry;
      while ((entry = zis.getNextEntry()) != null) {
        if (!entry.getName().equals(filename)) {
          continue;
        }

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        while ((bytesRead = zis.read(buffer)) != -1) {
          output.write(buffer, 0, bytesRead);
        }
        byte[] fileData = output.toByteArray();

        return objectMapper.readValue(fileData, valueType);
      }
    } catch (Exception e) {
      log.error("ThemeRegisterService:extractFile error: {}", e.getMessage());
      throw new RuntimeException(e);
    }

    log.error("ThemeRegisterService:registerTheme {} is null", filename);
    throw new RuntimeException(filename + " is null");
  }

  private record ExtractResult(
      ThemeManifest manifest,
      Map<String, JsonNode> themeSchemas,
      Map<String, JsonNode> uiSchemas) {}
}
