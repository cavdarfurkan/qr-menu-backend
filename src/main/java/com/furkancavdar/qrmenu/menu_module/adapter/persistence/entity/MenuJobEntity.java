package com.furkancavdar.qrmenu.menu_module.adapter.persistence.entity;

import com.furkancavdar.qrmenu.menu_module.domain.MenuJobStatus;
import com.furkancavdar.qrmenu.menu_module.domain.MenuJobType;
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

  @Enumerated(EnumType.STRING)
  @Column(name = "type", nullable = false)
  private MenuJobType type;

  @Column(name = "timestamp", nullable = false)
  private Long timestamp;

  @ManyToOne
  @JoinColumn(name = "menu_id")
  private MenuEntity menu;
}
