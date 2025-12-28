package com.furkancavdar.qrmenu.menu_module.adapter.persistence.repository;

import com.furkancavdar.qrmenu.menu_module.adapter.persistence.entity.MenuContentItemEntity;
import com.furkancavdar.qrmenu.menu_module.adapter.persistence.entity.MenuContentRelationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public interface JpaMenuContentRelationRepository extends JpaRepository<MenuContentRelationEntity, Long> {
    @Modifying(flushAutomatically = true)
    @Query("DELETE FROM MenuContentRelationEntity r WHERE r.sourceItem = ?1 AND r.fieldName = ?2")
    void deleteBySourceItemAndFieldName(MenuContentItemEntity sourceItem, String fieldName);

    List<MenuContentRelationEntity> findBySourceItem_Id(UUID sourceItemId);

    List<MenuContentRelationEntity> findBySourceItem_IdAndFieldNameOrderByPositionAsc(UUID sourceItemId, String fieldName);

    List<MenuContentRelationEntity> findByTargetItem_Id(UUID targetItemId);

    boolean existsByTargetItem_Id(UUID targetItemId);

    @Query("SELECT DISTINCT r.targetItem.id FROM MenuContentRelationEntity r WHERE r.targetItem.id IN :targetItemIds")
    Set<UUID> findReferencedTargetItemIds(Set<UUID> targetItemIds);
}
