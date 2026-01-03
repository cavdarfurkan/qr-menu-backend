package com.furkancavdar.qrmenu.menu_module.application.service;

import com.furkancavdar.qrmenu.common.exception.ResourceNotFoundException;
import com.furkancavdar.qrmenu.menu_module.application.port.in.MenuJobUseCase;
import com.furkancavdar.qrmenu.menu_module.application.port.out.MenuJobRepositoryPort;
import com.furkancavdar.qrmenu.menu_module.application.port.out.MenuRepositoryPort;
import com.furkancavdar.qrmenu.menu_module.domain.MenuJob;
import com.furkancavdar.qrmenu.menu_module.domain.MenuJobStatus;
import com.furkancavdar.qrmenu.menu_module.domain.MenuJobType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class MenuJobService implements MenuJobUseCase {

  private final MenuJobRepositoryPort menuJobRepository;
  private final MenuRepositoryPort menuRepository;

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
    Boolean result = menuJobRepository.updateStatus(jobId, status);

    // If job completed successfully, update menu publish status
    if (result && status == MenuJobStatus.DONE) {
      MenuJob menuJob = menuJobRepository.findById(jobId).orElse(null);
      if (menuJob != null && menuJob.getMenuId() != null && menuJob.getType() != null) {
        try {
          if (menuJob.getType() == MenuJobType.BUILD) {
            // Build job completed - set menu as published
            menuRepository.updatePublishedStatus(menuJob.getMenuId(), true);
            log.info(
                "Menu {} marked as published after build job {} completed",
                menuJob.getMenuId(),
                jobId);
          } else if (menuJob.getType() == MenuJobType.UNPUBLISH) {
            // Unpublish job completed - set menu as unpublished
            menuRepository.updatePublishedStatus(menuJob.getMenuId(), false);
            log.info(
                "Menu {} marked as unpublished after unpublish job {} completed",
                menuJob.getMenuId(),
                jobId);
          }
        } catch (Exception e) {
          // Log error but don't fail job status update
          log.error(
              "Failed to update menu publish status for menu {} after job {}: {}",
              menuJob.getMenuId(),
              jobId,
              e.getMessage());
        }
      }
    }

    return result;
  }
}
