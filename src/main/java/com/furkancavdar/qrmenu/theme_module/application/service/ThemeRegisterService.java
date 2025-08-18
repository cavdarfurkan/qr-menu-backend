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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

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
            themeDto.setThemeSchemas(extractResult.schemas);
        } catch (IOException e) {
            log.error("ThemeRegisterService.registerTheme error: {}", e.getMessage());
            throw new RuntimeException(e.getMessage());
        }

        Optional<Theme> optionalTheme = themeRepository.findByThemeName(themeDto.getThemeManifest().getName());
        if (optionalTheme.isPresent()) {
            throw new RuntimeException("Theme already exists");
        }

        String location = themeStorage.putTheme(new ByteArrayInputStream(zipBytes), themeDto.getThemeManifest().getName());
        String thumbnailLocation = null;
        if (thumbnailIs != null) {
            thumbnailLocation = themeStorage.putThumbnail(thumbnailIs, themeDto.getThemeManifest().getName());
        }

        Theme theme = ThemeDtoMapper.toTheme(themeDto, thumbnailLocation != null ? thumbnailLocation : PLACEHOLDER_THUMBNAIL, location);
        themeRepository.save(theme);
        log.info("Theme registered: {}", themeDto);
    }

    @Override
    public void unregisterTheme(Long themeId, String ownerName, Boolean isAdmin) {
        Theme theme = themeRepository.findById(themeId).orElseThrow(() -> new RuntimeException("Theme not found"));

        if (!isAdmin && !theme.isOwner(ownerName)) {
            throw new RuntimeException(ownerName + " is not the owner of theme " + theme.getThemeManifest().getName());
        }

        log.info("ThemeRegisterService:unregisterTheme theme: {}", theme.toString());

        themeRepository.deleteThemeById(theme.getId());
        themeStorage.deleteTheme(theme);

        log.info("Theme unregistered: {}", theme);
    }

    @Override
    public ThemeManifestResultDto getManifest(Long themeId) {
        Theme theme = themeRepository.findById(themeId).orElseThrow(() -> new ResourceNotFoundException("Theme not found"));
        return new ThemeManifestResultDto(theme.getThemeManifest());
    }

    @Override
    public ThemeSchemasResultDto getSchemas(Long themeId, List<String> refs) {
        Theme theme = themeRepository.findById(themeId).orElseThrow(() -> new ResourceNotFoundException("Theme not found"));

        Map<String, JsonNode> schemas = theme.getSchemas();
        if (schemas == null) {
            return new ThemeSchemasResultDto(0, null);
        }

        if (refs == null || refs.isEmpty()) {
            return new ThemeSchemasResultDto(schemas.size(), schemas);
        }

        Map<String, JsonNode> filteredSchemas = new HashMap<>();
        schemas.forEach((k, v) -> {
            if (refs.contains(k)) {
                filteredSchemas.put(k, v);
            }
        });
        return new ThemeSchemasResultDto(filteredSchemas.size(), filteredSchemas);
    }

    @Override
    public Page<ThemeDto> getAllThemes(Integer page, Integer size) {
        return themeRepository.getAllThemes(page, size).map(ThemeDtoMapper::toThemeDto);
    }

    private ExtractResult extractManifestAndSchemas(byte[] zipBytes) {
        if (!containsManifest(new ByteArrayInputStream(zipBytes))) {
            throw new RuntimeException("Zip file does not contain manifest");
        }

        ThemeManifest manifest = extractThemeManifest(new ByteArrayInputStream(zipBytes));
        if (manifest == null) {
            log.error("ThemeRegisterService:registerTheme themeManifest is null");
            throw new RuntimeException("Theme manifest is null");
        }

        Map<String, JsonNode> schemas = manifest.getSchemasLocation().stream()
                .map(node -> extractSchemas(new ByteArrayInputStream(zipBytes), node.get("path").asText()))
                .collect(Collectors.toMap(node -> {
                    if (node == null) {
                        throw new RuntimeException("Schema location is null");
                    }
                    String[] refParts = node.get("$ref").asText().split("/");
                    return refParts[refParts.length - 1];
                }, Function.identity()));

        return new ExtractResult(manifest, schemas);
    }

    private Boolean containsManifest(InputStream is) {
        try (ZipInputStream zis = new ZipInputStream(is)) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                if ("manifest.json".equals(entry.getName())) {
                    return Boolean.TRUE;
                }
            }
        } catch (Exception e) {
            log.error("ThemeControllerV1:containsThemeConfig error {}", e.getMessage());
        }
        return Boolean.FALSE;
    }

    private ThemeManifest extractThemeManifest(InputStream is) {
        try (ZipInputStream zis = new ZipInputStream(is)) {
            byte[] buffer = new byte[1024]; // 1 KB
            int bytesRead;

            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                if (!entry.getName().equals("manifest.json")) {
                    continue;
                }

                ByteArrayOutputStream output = new ByteArrayOutputStream();
                while ((bytesRead = zis.read(buffer)) != -1) {
                    output.write(buffer, 0, bytesRead);
                }
                byte[] fileData = output.toByteArray();

                return objectMapper.readValue(fileData, ThemeManifest.class);
            }
        } catch (Exception e) {
            log.error("ThemeRegisterService:getThemeManifest error", e);
            throw new RuntimeException(e);
        }

        return null;
    }

    private JsonNode extractSchemas(InputStream is, String location) {
        try (ZipInputStream zis = new ZipInputStream(is)) {
            byte[] buffer = new byte[1024]; // 1 KB
            int bytesRead;

            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                if (!entry.getName().equals(location)) {
                    continue;
                }

                ByteArrayOutputStream output = new ByteArrayOutputStream();
                while ((bytesRead = zis.read(buffer)) != -1) {
                    output.write(buffer, 0, bytesRead);
                }
                byte[] fileData = output.toByteArray();

                return objectMapper.readValue(fileData, JsonNode.class);
            }
        } catch (Exception e) {
            log.error("ThemeRegisterService:extractSchemas error", e);
            throw new RuntimeException(e);
        }

        return null;
    }

    private record ExtractResult(ThemeManifest manifest, Map<String, JsonNode> schemas) {
    }
}
