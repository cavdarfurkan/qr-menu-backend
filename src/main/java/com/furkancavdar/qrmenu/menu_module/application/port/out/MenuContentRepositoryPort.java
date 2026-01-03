package com.furkancavdar.qrmenu.menu_module.application.port.out;

import com.furkancavdar.qrmenu.menu_module.domain.MenuContentItem;
import com.furkancavdar.qrmenu.menu_module.domain.MenuContentRelation;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface MenuContentRepositoryPort {

  /*
   * ###############
   * MenuContentItem
   * ###############
   */

  MenuContentItem save(MenuContentItem menuContentItem);

  void delete(MenuContentItem menuContentItem);

  void deleteAll(List<MenuContentItem> menuContentItems);

  Optional<MenuContentItem> findById(UUID itemId);

  List<MenuContentItem> findByMenuIdAndCollectionName(Long menuId, String collectionName);

  Optional<MenuContentItem> findByMenuIdAndCollectionNameAndId(
      Long menuId, String collectionName, UUID itemId);

  List<MenuContentItem> findByMenuId(Long menuId);

  List<MenuContentItem> findAllByIdIn(Set<UUID> ids);

  /*
   * ###################
   * MenuContentRelation
   * ###################
   */

  void deleteBySourceAndField(MenuContentItem sourceItem, String fieldName);

  MenuContentRelation save(MenuContentRelation menuContentRelation);

  List<MenuContentRelation> findBySourceItemId(UUID sourceItemId);

  List<MenuContentRelation> findBySourceItemIdAndFieldNameOrderByPositionAsc(
      UUID sourceItemId, String fieldName);

  List<MenuContentRelation> findByTargetItemId(UUID targetItemId);

  boolean existsByTargetItemId(UUID targetItemId);

  Set<UUID> findReferencedTargetItemIds(Set<UUID> targetItemIds);

  void deleteRelationsByItemIds(Set<UUID> itemIds);
}
