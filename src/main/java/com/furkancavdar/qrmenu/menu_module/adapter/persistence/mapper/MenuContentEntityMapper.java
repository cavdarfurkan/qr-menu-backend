package com.furkancavdar.qrmenu.menu_module.adapter.persistence.mapper;

import com.furkancavdar.qrmenu.menu_module.adapter.persistence.entity.MenuContentItemEntity;
import com.furkancavdar.qrmenu.menu_module.adapter.persistence.entity.MenuContentRelationEntity;
import com.furkancavdar.qrmenu.menu_module.domain.MenuContentItem;
import com.furkancavdar.qrmenu.menu_module.domain.MenuContentRelation;
import com.furkancavdar.qrmenu.theme_module.adapter.persistence.mapper.ThemeEntityMapper;

public class MenuContentEntityMapper {

  private MenuContentEntityMapper() {}

  /*
   * ###############
   * MenuContentItem
   * ###############
   */

  public static MenuContentItemEntity toMenuContentItemEntity(MenuContentItem menuContentItem) {
    if (menuContentItem == null) {
      return null;
    }

    MenuContentItemEntity menuContentItemEntity = new MenuContentItemEntity();
    menuContentItemEntity.setId(menuContentItem.getId());
    menuContentItemEntity.setMenu(MenuEntityMapper.toMenuEntity(menuContentItem.getMenu()));
    menuContentItemEntity.setOwnerId(menuContentItem.getOwnerId());
    menuContentItemEntity.setTheme(ThemeEntityMapper.toThemeEntity(menuContentItem.getTheme()));
    menuContentItemEntity.setCollectionName(menuContentItem.getCollectionName());
    menuContentItemEntity.setData(menuContentItem.getData());
    return menuContentItemEntity;
  }

  public static MenuContentItem toMenuContentItem(MenuContentItemEntity menuContentItemEntity) {
    if (menuContentItemEntity == null) {
      return null;
    }

    return MenuContentItem.builder()
        .id(menuContentItemEntity.getId())
        .menu(MenuEntityMapper.toMenu(menuContentItemEntity.getMenu()))
        .ownerId(menuContentItemEntity.getOwnerId())
        .theme(ThemeEntityMapper.toTheme(menuContentItemEntity.getTheme()))
        .collectionName(menuContentItemEntity.getCollectionName())
        .data(menuContentItemEntity.getData())
        .build();
  }

  /*
   * ###################
   * MenuContentRelation
   * ###################
   */

  public static MenuContentRelationEntity toMenuContentRelationEntity(
      MenuContentRelation menuContentRelation) {
    if (menuContentRelation == null) {
      return null;
    }

    MenuContentRelationEntity menuContentRelationEntity = new MenuContentRelationEntity();
    menuContentRelationEntity.setId(menuContentRelation.getId());
    menuContentRelationEntity.setSourceItem(
        MenuContentEntityMapper.toMenuContentItemEntity(menuContentRelation.getSourceItem()));
    menuContentRelationEntity.setFieldName(menuContentRelation.getFieldName());
    menuContentRelationEntity.setTargetItem(
        MenuContentEntityMapper.toMenuContentItemEntity(menuContentRelation.getTargetItem()));
    menuContentRelationEntity.setPosition(menuContentRelation.getPosition());
    return menuContentRelationEntity;
  }

  public static MenuContentRelation toMenuContentRelation(
      MenuContentRelationEntity menuContentRelationEntity) {
    if (menuContentRelationEntity == null) {
      return null;
    }

    return MenuContentRelation.builder()
        .id(menuContentRelationEntity.getId())
        .sourceItem(
            MenuContentEntityMapper.toMenuContentItem(menuContentRelationEntity.getSourceItem()))
        .fieldName(menuContentRelationEntity.getFieldName())
        .targetItem(
            MenuContentEntityMapper.toMenuContentItem(menuContentRelationEntity.getTargetItem()))
        .position(menuContentRelationEntity.getPosition())
        .build();
  }
}
