package com.furkancavdar.qrmenu.menu_module.adapter.persistence.repository;

import com.furkancavdar.qrmenu.menu_module.adapter.persistence.mapper.MenuContentEntityMapper;
import com.furkancavdar.qrmenu.menu_module.application.port.out.MenuContentRepositoryPort;
import com.furkancavdar.qrmenu.menu_module.domain.MenuContentItem;
import com.furkancavdar.qrmenu.menu_module.domain.MenuContentRelation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class MenuContentAdapter implements MenuContentRepositoryPort {

    private final JpaMenuContentItemRepository jpaMenuContentItemRepository;
    private final JpaMenuContentRelationRepository jpaMenuContentRelationRepository;

    /*
     * ###############
     * MenuContentItem
     * ###############
     */

    @Override
    public MenuContentItem save(MenuContentItem menuContentItem) {
        return MenuContentEntityMapper.toMenuContentItem(
                jpaMenuContentItemRepository.save(MenuContentEntityMapper.toMenuContentItemEntity(menuContentItem))
        );
    }

    @Override
    public void delete(MenuContentItem menuContentItem) {
        jpaMenuContentItemRepository.delete(
                MenuContentEntityMapper.toMenuContentItemEntity(menuContentItem)
        );
    }

    @Override
    public Optional<MenuContentItem> findById(UUID itemId) {
        return jpaMenuContentItemRepository.findById(itemId).map(MenuContentEntityMapper::toMenuContentItem);
    }

    @Override
    public List<MenuContentItem> findByMenuIdAndCollectionName(Long menuId, String collectionName) {
        return jpaMenuContentItemRepository.findByMenu_IdAndCollectionName(menuId, collectionName)
                .stream().map(MenuContentEntityMapper::toMenuContentItem).toList();
    }

    @Override
    public Optional<MenuContentItem> findByMenuIdAndCollectionNameAndId(Long menuId, String collectionName, UUID itemId) {
        return jpaMenuContentItemRepository.findByMenu_IdAndCollectionNameAndId(menuId, collectionName, itemId)
                .map(MenuContentEntityMapper::toMenuContentItem);
    }

    @Override
    public List<MenuContentItem> findAllByIdIn(Set<UUID> ids) {
        return jpaMenuContentItemRepository.findAllByIdIn(ids).stream()
                .map(MenuContentEntityMapper::toMenuContentItem)
                .toList();
    }

    /*
     * ###################
     * MenuContentRelation
     * ###################
     */

    @Override
    public void deleteBySourceAndField(MenuContentItem sourceItem, String fieldName) {
        jpaMenuContentRelationRepository.deleteBySourceItemAndFieldName(MenuContentEntityMapper.toMenuContentItemEntity(sourceItem), fieldName);
    }

    @Override
    public MenuContentRelation save(MenuContentRelation menuContentRelation) {
        return MenuContentEntityMapper.toMenuContentRelation(
                jpaMenuContentRelationRepository.save(MenuContentEntityMapper.toMenuContentRelationEntity(menuContentRelation))
        );
    }

    @Override
    public List<MenuContentRelation> findBySourceItemId(UUID sourceItemId) {
        return jpaMenuContentRelationRepository.findBySourceItem_Id(sourceItemId).stream()
                .map(MenuContentEntityMapper::toMenuContentRelation)
                .toList();
    }

    @Override
    public List<MenuContentRelation> findBySourceItemIdAndFieldNameOrderByPositionAsc(UUID sourceItemId, String fieldName) {
        return jpaMenuContentRelationRepository.findBySourceItem_IdAndFieldNameOrderByPositionAsc(sourceItemId, fieldName).stream()
                .map(MenuContentEntityMapper::toMenuContentRelation)
                .toList();
    }
}