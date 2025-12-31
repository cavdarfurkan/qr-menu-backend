package com.furkancavdar.qrmenu.auth.adapter.persistence.repository;

import com.furkancavdar.qrmenu.auth.adapter.persistence.entity.UserEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface JpaUserRepository extends JpaRepository<UserEntity, Long> {
  Optional<UserEntity> findByUsername(String username);

  Optional<UserEntity> findByEmail(String email);

  boolean existsByUsername(String username);

  boolean existsByEmail(String email);

  @Transactional
  @Modifying
  @Query("update UserEntity u set u.password = ?1 where u.id = ?2")
  int updatePasswordById(String password, Long id);
}
