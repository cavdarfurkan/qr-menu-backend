package com.furkancavdar.qrmenu.auth.application.port.out;

import com.furkancavdar.qrmenu.auth.domain.User;
import java.util.Optional;

public interface UserRepositoryPort {
  User save(User user);

  Optional<User> findById(Long id);

  Optional<User> findByUsername(String username);

  Optional<User> findByEmail(String email);

  boolean existsByUsername(String username);

  boolean existsByEmail(String email);

  int updatePassword(Long userId, String newPassword);
}
