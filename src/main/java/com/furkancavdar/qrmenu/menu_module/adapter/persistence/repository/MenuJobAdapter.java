package com.furkancavdar.qrmenu.menu_module.adapter.persistence.repository;

import com.furkancavdar.qrmenu.menu_module.adapter.persistence.mapper.MenuJobEntityMapper;
import com.furkancavdar.qrmenu.menu_module.application.port.out.MenuJobRepositoryPort;
import com.furkancavdar.qrmenu.menu_module.domain.MenuJob;
import com.furkancavdar.qrmenu.menu_module.domain.MenuJobStatus;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class MenuJobAdapter implements MenuJobRepositoryPort {

  private final JpaMenuJobRepository jpaMenuJobRepository;

  @Override
  public void upsert(MenuJob menuJob) {
    log.info("Saving menu job: {}", menuJob);
    jpaMenuJobRepository.save(MenuJobEntityMapper.toMenuJobEntity(menuJob));
  }

  @Override
  public Optional<MenuJob> findById(String id) {
    return jpaMenuJobRepository.findById(id).map(MenuJobEntityMapper::toMenuJob);
  }

  @Override
  public Boolean updateStatus(String id, MenuJobStatus menuJobStatus) {
    MenuJob menuJob = this.findById(id).orElse(null);
    if (menuJob == null) {
      return Boolean.FALSE;
    }
    menuJob.setStatus(menuJobStatus);
    this.upsert(menuJob);
    return Boolean.TRUE;
  }
}
