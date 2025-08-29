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

    Optional<MenuContentItem> findById(UUID itemId);

    List<MenuContentItem> findByMenuIdAndCollectionName(Long menuId, String collectionName);

    List<MenuContentItem> findAllByIdIn(Set<UUID> ids);

    /*
     * ###################
     * MenuContentRelation
     * ###################
     */

    void deleteBySourceAndField(MenuContentItem sourceItem, String fieldName);

    MenuContentRelation save(MenuContentRelation menuContentRelation);

    List<MenuContentRelation> findBySourceItemId(UUID sourceItemId);

    List<MenuContentRelation> findBySourceItemIdAndFieldNameOrderByPositionAsc(UUID sourceItemId, String fieldName);
}
