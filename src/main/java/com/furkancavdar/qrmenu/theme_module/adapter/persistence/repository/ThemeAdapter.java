package com.furkancavdar.qrmenu.theme_module.adapter.persistence.repository;

import com.furkancavdar.qrmenu.theme_module.adapter.persistence.mapper.ThemeEntityMapper;
import com.furkancavdar.qrmenu.theme_module.application.port.out.ThemeRepositoryPort;
import com.furkancavdar.qrmenu.theme_module.domain.Theme;
import com.furkancavdar.qrmenu.theme_module.domain.ThemeCategory;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ThemeAdapter implements ThemeRepositoryPort {

  private final JpaThemeRepository jpaThemeRepository;

  @Override
  public Theme save(Theme theme) {
    return ThemeEntityMapper.toTheme(
        jpaThemeRepository.save(ThemeEntityMapper.toThemeEntity(theme)));
  }

  @Override
  public Optional<Theme> findById(Long id) {
    log.info("ThemeAdapter:findById");
    return Optional.ofNullable(
        ThemeEntityMapper.toTheme(jpaThemeRepository.findById(id).orElse(null)));
  }

  @Override
  public Optional<Theme> findByThemeName(String themeName) {
    log.info("ThemeAdapter:findByThemeName: {}", themeName);
    return jpaThemeRepository.findByThemeManifest_Name(themeName).map(ThemeEntityMapper::toTheme);
  }

  @Override
  public void deleteThemeById(Long id) {
    log.info("ThemeAdapter:deleteThemeById");
    jpaThemeRepository.deleteById(id);
  }

  @Override
  public Page<Theme> getAllThemes(Integer page, Integer size, ThemeCategory category) {
    PageRequest pageRequest = PageRequest.of(page, size);
    if (category != null) {
      return jpaThemeRepository
          .findByCategory(category, pageRequest)
          .map(ThemeEntityMapper::toTheme);
    }
    return jpaThemeRepository.findAll(pageRequest).map(ThemeEntityMapper::toTheme);
  }

  @Override
  public Page<Theme> findByOwnerUsername(
      String username, Integer page, Integer size, ThemeCategory category) {
    log.info("ThemeAdapter:findByOwnerUsername: {}, category: {}", username, category);
    PageRequest pageRequest = PageRequest.of(page, size);
    if (category != null) {
      return jpaThemeRepository
          .findByOwner_UsernameAndCategory(username, category, pageRequest)
          .map(ThemeEntityMapper::toTheme);
    }
    return jpaThemeRepository
        .findByOwner_Username(username, pageRequest)
        .map(ThemeEntityMapper::toTheme);
  }
}
