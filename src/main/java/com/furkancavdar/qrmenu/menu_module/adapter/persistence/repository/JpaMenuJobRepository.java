package com.furkancavdar.qrmenu.menu_module.adapter.persistence.repository;

import com.furkancavdar.qrmenu.menu_module.adapter.persistence.entity.MenuJobEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaMenuJobRepository extends JpaRepository<MenuJobEntity, String> {}
