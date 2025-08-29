package com.furkancavdar.qrmenu.menu_module.adapter.persistence.repository;

import com.furkancavdar.qrmenu.menu_module.adapter.persistence.entity.MenuContentItemEntity;
import com.furkancavdar.qrmenu.menu_module.adapter.persistence.entity.MenuContentRelationEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface JpaMenuContentRelationRepository extends JpaRepository<MenuContentRelationEntity, Long> {
    void deleteBySourceItemAndFieldName(MenuContentItemEntity sourceItem, String fieldName);

    List<MenuContentRelationEntity> findBySourceItem_Id(UUID sourceItemId);

    List<MenuContentRelationEntity> findBySourceItem_IdAndFieldNameOrderByPositionAsc(UUID sourceItemId, String fieldName);
}