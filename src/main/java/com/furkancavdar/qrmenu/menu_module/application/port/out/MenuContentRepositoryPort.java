package com.furkancavdar.qrmenu.menu_module.application.port.out;

import com.furkancavdar.qrmenu.menu_module.domain.MenuContent;

import java.util.Optional;

public interface MenuContentRepositoryPort {
    MenuContent save(MenuContent menuContent);

    void delete(MenuContent menuContent);

    Optional<MenuContent> findByMenuIdAndCollectionName(Long menuId, String collectionName);
}
