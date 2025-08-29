package com.furkancavdar.qrmenu.menu_module.adapter.persistence.repository;

import com.furkancavdar.qrmenu.menu_module.adapter.persistence.entity.MenuContentItemEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public interface JpaMenuContentItemRepository extends JpaRepository<MenuContentItemEntity, UUID> {

//    Optional<MenuContentItemEntity> findByMenu_IdAndCollectionName(Long id, String collectionName);

//    @Query("select m from MenuContentItemEntity m where m.id in ?1")
//    List<MenuContentItemEntity> findAllByIdIn(Collection<UUID> ids);


    List<MenuContentItemEntity> findByMenu_IdAndCollectionName(Long menuId, String collectionName);

    List<MenuContentItemEntity> findAllByIdIn(Set<UUID> ids);
}