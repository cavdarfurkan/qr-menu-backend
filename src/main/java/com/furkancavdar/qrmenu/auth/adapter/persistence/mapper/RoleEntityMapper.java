package com.furkancavdar.qrmenu.auth.adapter.persistence.mapper;

import com.furkancavdar.qrmenu.auth.adapter.persistence.entity.RoleEntity;
import com.furkancavdar.qrmenu.auth.domain.Role;

public class RoleEntityMapper {

    private RoleEntityMapper() {
        // Private constructor to prevent instantiation
    }

    public static RoleEntity toEntity(Role domainRole) {
        if (domainRole == null) {
            return null;
        }

        RoleEntity entity = new RoleEntity();
        entity.setId(domainRole.id());
        entity.setName(domainRole.name());
        return entity;
    }

    public static Role toDomain(RoleEntity entity) {
        if (entity == null) {
            return null;
        }

        return new Role(entity.getId(), entity.getName());
    }
} 