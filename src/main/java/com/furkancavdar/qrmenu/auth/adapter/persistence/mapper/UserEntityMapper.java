package com.furkancavdar.qrmenu.auth.adapter.persistence.mapper;

import com.furkancavdar.qrmenu.auth.adapter.persistence.entity.UserEntity;
import com.furkancavdar.qrmenu.auth.domain.Role;
import com.furkancavdar.qrmenu.auth.domain.User;

import java.util.stream.Collectors;

public class UserEntityMapper {

    private UserEntityMapper() {
        // Private constructor to prevent instantiation
    }

    public static UserEntity toEntity(User domainUser) {
        if (domainUser == null) {
            return null;
        }

        UserEntity entity = new UserEntity();
        entity.setId(domainUser.getId());
        entity.setUsername(domainUser.getUsername());
        entity.setPassword(domainUser.getPassword());
        entity.setEmail(domainUser.getEmail());
        entity.setEnabled(true);
        entity.setAccountNonExpired(true);
        entity.setAccountNonLocked(true);
        entity.setCredentialsNonExpired(true);

        if (domainUser.getRoles() != null) {
            entity.setRoles(domainUser.getRoles().stream()
                    .map(RoleEntityMapper::toEntity)
                    .collect(Collectors.toSet()));
        }

        return entity;
    }

    public static User toDomain(UserEntity entity) {
        if (entity == null) {
            return null;
        }

        User user = new User(entity.getId(), entity.getUsername(), entity.getPassword(), entity.getEmail());

        if (entity.getRoles() != null) {
            entity.getRoles().forEach(roleEntity -> {
                Role role = RoleEntityMapper.toDomain(roleEntity);
                user.addRole(role);
            });
        }

        return user;
    }
} 