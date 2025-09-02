package com.furkancavdar.qrmenu.menu_module.domain;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@Builder
public class MenuContentRelation {
    private Long id;
    private final MenuContentItem sourceItem;
    private final String fieldName;
    private final MenuContentItem targetItem;
    private final Integer position;

    public MenuContentRelation(MenuContentItem sourceItem, String fieldName, MenuContentItem targetItem, Integer position) {
        this.sourceItem = sourceItem;
        this.fieldName = fieldName;
        this.targetItem = targetItem;
        this.position = position;
    }

    public MenuContentRelation(Long id, MenuContentItem sourceItem, String fieldName, MenuContentItem targetItem, Integer position) {
        this(sourceItem, fieldName, targetItem, position);
        this.id = id;
    }
}
