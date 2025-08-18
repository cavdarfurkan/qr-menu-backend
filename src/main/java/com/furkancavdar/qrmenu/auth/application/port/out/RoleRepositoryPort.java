package com.furkancavdar.qrmenu.auth.application.port.out;

import com.furkancavdar.qrmenu.auth.domain.Role;

import java.util.Optional;

public interface RoleRepositoryPort {
    Optional<Role> findByName(String name);
} 