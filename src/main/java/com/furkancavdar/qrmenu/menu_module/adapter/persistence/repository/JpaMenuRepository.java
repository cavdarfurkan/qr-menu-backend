package com.furkancavdar.qrmenu.menu_module.adapter.persistence.repository;

import com.furkancavdar.qrmenu.menu_module.adapter.persistence.entity.MenuEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JpaMenuRepository extends JpaRepository<MenuEntity, Long> {
    List<MenuEntity> findByOwner_Id(Long id);
}