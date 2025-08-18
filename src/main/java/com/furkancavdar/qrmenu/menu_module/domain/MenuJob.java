package com.furkancavdar.qrmenu.menu_module.domain;

import com.furkancavdar.qrmenu.menu_module.domain.MenuJobStatus;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@ToString
public class MenuJob {
    private String id;

    @Setter
    private MenuJobStatus status;

    public MenuJob(MenuJobStatus status) {
        this.status = status;
    }

    public MenuJob(String id, MenuJobStatus status) {
        this(status);
        this.id = id;
    }
}
