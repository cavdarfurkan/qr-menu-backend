package com.furkancavdar.qrmenu.theme_module.adapter.persistence.repository;

import com.furkancavdar.qrmenu.theme_module.adapter.persistence.entity.ThemeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface JpaThemeRepository extends JpaRepository<ThemeEntity, Long> {
    Optional<ThemeEntity> findByThemeManifest_Name(String themeName);
}