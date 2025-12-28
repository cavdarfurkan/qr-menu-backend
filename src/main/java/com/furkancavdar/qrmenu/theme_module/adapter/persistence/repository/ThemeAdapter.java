package com.furkancavdar.qrmenu.theme_module.adapter.persistence.repository;

import com.furkancavdar.qrmenu.theme_module.adapter.persistence.mapper.ThemeEntityMapper;
import com.furkancavdar.qrmenu.theme_module.application.port.out.ThemeRepositoryPort;
import com.furkancavdar.qrmenu.theme_module.domain.Theme;
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
  public Page<Theme> getAllThemes(Integer page, Integer size) {
    return jpaThemeRepository.findAll(PageRequest.of(page, size)).map(ThemeEntityMapper::toTheme);
  }
}
