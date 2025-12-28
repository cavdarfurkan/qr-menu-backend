package com.furkancavdar.qrmenu.menu_module.application.port.out;

import com.furkancavdar.qrmenu.menu_module.domain.Menu;
import java.util.List;
import java.util.Optional;

public interface MenuRepositoryPort {
  Menu save(Menu menu);

  void delete(Menu menu);

  Optional<Menu> findById(Long id);

  List<Menu> findAllByOwnerId(Long ownerId);
}
