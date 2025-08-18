package com.furkancavdar.qrmenu.auth.adapter.persistence.repository;

import com.furkancavdar.qrmenu.auth.adapter.persistence.entity.RoleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface JpaRoleRepository extends JpaRepository<RoleEntity, Long> {
    Optional<RoleEntity> findByName(String name);
} 