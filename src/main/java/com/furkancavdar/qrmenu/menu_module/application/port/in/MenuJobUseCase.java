package com.furkancavdar.qrmenu.menu_module.application.port.in;

import com.furkancavdar.qrmenu.menu_module.domain.MenuJob;
import com.furkancavdar.qrmenu.menu_module.domain.MenuJobStatus;

public interface MenuJobUseCase {
    void save(MenuJob menuJob);

    MenuJobStatus getJobStatus(String jobId);

    Boolean updateJobStatus(String jobId, MenuJobStatus status);
}
