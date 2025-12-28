package com.furkancavdar.qrmenu.menu_module.application.port.out;

import com.furkancavdar.qrmenu.menu_module.domain.MenuJob;
import com.furkancavdar.qrmenu.menu_module.domain.MenuJobStatus;
import java.util.Optional;

public interface MenuJobRepositoryPort {
  void upsert(MenuJob menuJob);

  Optional<MenuJob> findById(String id);

  Boolean updateStatus(String id, MenuJobStatus menuJobStatus);
}
