package com.furkancavdar.qrmenu.auth.application.port.out;

import com.furkancavdar.qrmenu.auth.domain.User;
import java.util.Optional;

public interface UserRepositoryPort {
  User save(User user);

  Optional<User> findByUsername(String username);

  boolean existsByUsername(String username);

  boolean existsByEmail(String email);
}
