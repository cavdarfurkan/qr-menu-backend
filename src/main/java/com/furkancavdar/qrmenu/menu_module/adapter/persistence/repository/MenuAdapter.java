package com.furkancavdar.qrmenu.menu_module.adapter.persistence.repository;

import com.furkancavdar.qrmenu.menu_module.adapter.persistence.mapper.MenuEntityMapper;
import com.furkancavdar.qrmenu.menu_module.application.port.out.MenuRepositoryPort;
import com.furkancavdar.qrmenu.menu_module.domain.Menu;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class MenuAdapter implements MenuRepositoryPort {

  private final JpaMenuRepository jpaMenuRepository;

  @Override
  public Menu save(Menu menu) {
    log.info("MenuAdapter:save");
    return MenuEntityMapper.toMenu(jpaMenuRepository.save(MenuEntityMapper.toMenuEntity(menu)));
  }

  @Override
  public void delete(Menu menu) {
    jpaMenuRepository.delete(MenuEntityMapper.toMenuEntity(menu));
  }

  @Override
  public Optional<Menu> findById(Long id) {
    return jpaMenuRepository.findById(id).map(MenuEntityMapper::toMenu);
  }

  @Override
  public List<Menu> findAllByOwnerId(Long ownerId) {
    return jpaMenuRepository.findByOwner_Id(ownerId).stream()
        .map(MenuEntityMapper::toMenu)
        .toList();
  }
}
