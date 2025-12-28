package com.furkancavdar.qrmenu.menu_module.adapter.persistence.entity;

import com.furkancavdar.qrmenu.auth.adapter.persistence.entity.UserEntity;
import com.furkancavdar.qrmenu.theme_module.adapter.persistence.entity.ThemeEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "menu")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MenuEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id", nullable = false)
  private Long id;

  @Column(name = "menu_name", nullable = false)
  private String menuName;

  @ManyToOne(optional = false)
  @JoinColumn(name = "owner_id", nullable = false)
  private UserEntity owner;

  @ManyToOne(optional = false)
  @JoinColumn(name = "selected_theme_id", nullable = false)
  private ThemeEntity selectedTheme;
}
