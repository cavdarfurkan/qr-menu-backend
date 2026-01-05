package com.furkancavdar.qrmenu.theme_module.adapter.persistence.repository;

import com.furkancavdar.qrmenu.theme_module.adapter.persistence.entity.ThemeEntity;
import com.furkancavdar.qrmenu.theme_module.domain.ThemeCategory;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JpaThemeRepository extends JpaRepository<ThemeEntity, Long> {
  Optional<ThemeEntity> findByThemeManifest_Name(String themeName);

  Page<ThemeEntity> findByOwner_Username(String username, Pageable pageable);

  Page<ThemeEntity> findByCategory(ThemeCategory category, Pageable pageable);

  Page<ThemeEntity> findByOwner_UsernameAndCategory(
      String username, ThemeCategory category, Pageable pageable);
}
