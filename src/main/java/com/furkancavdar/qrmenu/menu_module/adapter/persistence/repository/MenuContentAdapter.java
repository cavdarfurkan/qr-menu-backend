package com.furkancavdar.qrmenu.menu_module.adapter.persistence.repository;

import com.furkancavdar.qrmenu.menu_module.adapter.persistence.mapper.MenuContentEntityMapper;
import com.furkancavdar.qrmenu.menu_module.adapter.persistence.repository.JpaMenuContentRepository;
import com.furkancavdar.qrmenu.menu_module.application.port.out.MenuContentRepositoryPort;
import com.furkancavdar.qrmenu.menu_module.domain.MenuContent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class MenuContentAdapter implements MenuContentRepositoryPort {

    private final JpaMenuContentRepository jpaMenuContentRepository;

    @Override
    public MenuContent save(MenuContent menuContent) {
        return MenuContentEntityMapper.toMenuContent(
                jpaMenuContentRepository.save(MenuContentEntityMapper.toMenuContentEntity(menuContent))
        );
    }

    @Override
    public void delete(MenuContent menuContent) {
        jpaMenuContentRepository.delete(
                MenuContentEntityMapper.toMenuContentEntity(menuContent)
        );
    }

    @Override
    public Optional<MenuContent> findByMenuIdAndCollectionName(Long menuId, String collectionName) {
        return jpaMenuContentRepository.findByMenu_IdAndCollectionName(menuId, collectionName)
                .map(MenuContentEntityMapper::toMenuContent);
    }
}