package com.furkancavdar.qrmenu.menu_module.adapter.persistence.repository;

import com.furkancavdar.qrmenu.menu_module.adapter.persistence.entity.MenuContentItemEntity;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaMenuContentItemRepository extends JpaRepository<MenuContentItemEntity, UUID> {

  //    @Query("select m from MenuContentItemEntity m where m.id in ?1")
  //    List<MenuContentItemEntity> findAllByIdIn(Collection<UUID> ids);

  List<MenuContentItemEntity> findByMenu_IdAndCollectionName(Long menuId, String collectionName);

  Optional<MenuContentItemEntity> findByMenu_IdAndCollectionNameAndId(
      Long menuId, String collectionName, UUID itemId);

  List<MenuContentItemEntity> findByMenu_Id(Long menuId);

  List<MenuContentItemEntity> findAllByIdIn(Set<UUID> ids);
}
