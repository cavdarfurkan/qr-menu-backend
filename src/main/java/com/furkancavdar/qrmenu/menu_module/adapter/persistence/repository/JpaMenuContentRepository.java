package com.furkancavdar.qrmenu.menu_module.adapter.persistence.repository;

import com.furkancavdar.qrmenu.menu_module.adapter.persistence.entity.MenuContentEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface JpaMenuContentRepository extends JpaRepository<MenuContentEntity, Long> {
    Optional<MenuContentEntity> findByMenu_IdAndCollectionName(Long id, String collectionName);
}