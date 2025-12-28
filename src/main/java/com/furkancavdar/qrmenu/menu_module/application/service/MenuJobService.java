package com.furkancavdar.qrmenu.menu_module.application.service;

import com.furkancavdar.qrmenu.common.exception.ResourceNotFoundException;
import com.furkancavdar.qrmenu.menu_module.application.port.in.MenuJobUseCase;
import com.furkancavdar.qrmenu.menu_module.application.port.out.MenuJobRepositoryPort;
import com.furkancavdar.qrmenu.menu_module.domain.MenuJob;
import com.furkancavdar.qrmenu.menu_module.domain.MenuJobStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class MenuJobService implements MenuJobUseCase {

  private final MenuJobRepositoryPort menuJobRepository;

  @Override
  public void save(MenuJob menuJob) {
    menuJobRepository.upsert(menuJob);
  }

  @Override
  public MenuJobStatus getJobStatus(String jobId) {
    MenuJob menuJob =
        menuJobRepository
            .findById(jobId)
            .orElseThrow(
                () -> new ResourceNotFoundException("Menu Job with ID " + jobId + " not found"));
    return menuJob.getStatus();
  }

  @Override
  public Boolean updateJobStatus(String jobId, MenuJobStatus status) {
    return menuJobRepository.updateStatus(jobId, status);
  }
}
