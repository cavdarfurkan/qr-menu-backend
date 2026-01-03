package com.furkancavdar.qrmenu.menu_module.adapter.persistence.repository;

import com.furkancavdar.qrmenu.menu_module.adapter.persistence.entity.MenuEntity;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface JpaMenuRepository extends JpaRepository<MenuEntity, Long> {
  List<MenuEntity> findByOwner_Id(Long id);

  Optional<MenuEntity> findByCustomDomain(String customDomain);

  @Modifying
  @Transactional
  @Query("UPDATE MenuEntity m SET m.published = :published WHERE m.id = :menuId")
  void updatePublishedStatus(@Param("menuId") Long menuId, @Param("published") boolean published);
}
