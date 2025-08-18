package com.furkancavdar.qrmenu.menu_module.adapter.persistence.entity;

import com.furkancavdar.qrmenu.menu_module.domain.MenuJobStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "menu_job")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MenuJobEntity {
    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private String id;

    @Enumerated(EnumType.STRING)
    @Column(name = "menu_job_status", nullable = false)
    private MenuJobStatus menuJobStatus;
}
