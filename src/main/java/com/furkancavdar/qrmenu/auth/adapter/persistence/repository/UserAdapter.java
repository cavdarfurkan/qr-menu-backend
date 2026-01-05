package com.furkancavdar.qrmenu.auth.adapter.persistence.repository;

import com.furkancavdar.qrmenu.auth.adapter.persistence.mapper.RoleEntityMapper;
import com.furkancavdar.qrmenu.auth.adapter.persistence.mapper.UserEntityMapper;
import com.furkancavdar.qrmenu.auth.application.port.out.UserRepositoryPort;
import com.furkancavdar.qrmenu.auth.domain.User;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserAdapter implements UserRepositoryPort {

  private final JpaUserRepository jpaUserRepository;

  @Override
  public User save(User user) {
    return UserEntityMapper.toDomain(jpaUserRepository.save(UserEntityMapper.toEntity(user)));
  }

  @Override
  public Optional<User> findById(Long id) {
    return jpaUserRepository.findById(id).map(UserEntityMapper::toDomain);
  }

  @Override
  public Optional<User> findByUsername(String username) {
    return jpaUserRepository.findByUsername(username).map(UserEntityMapper::toDomain);
  }

  @Override
  public Optional<User> findByEmail(String email) {
    return jpaUserRepository.findByEmail(email).map(UserEntityMapper::toDomain);
  }

  @Override
  public boolean existsByUsername(String username) {
    return jpaUserRepository.existsByUsername(username);
  }

  @Override
  public boolean existsByEmail(String email) {
    return jpaUserRepository.existsByEmail(email);
  }

  @Override
  public int updatePassword(Long userId, String newPassword) {
    return jpaUserRepository.updatePasswordById(newPassword, userId);
  }

  @Override
  public User updateRoles(User user) {
    return jpaUserRepository
        .findById(user.getId())
        .map(
            entity -> {
              // Update only roles, preserving menus and themes
              entity.setRoles(
                  user.getRoles().stream()
                      .map(RoleEntityMapper::toEntity)
                      .collect(Collectors.toSet()));
              return UserEntityMapper.toDomain(jpaUserRepository.save(entity));
            })
        .orElseThrow(() -> new RuntimeException("User not found with id: " + user.getId()));
  }
}
