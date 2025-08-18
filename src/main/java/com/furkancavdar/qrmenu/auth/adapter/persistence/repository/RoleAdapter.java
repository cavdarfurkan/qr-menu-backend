package com.furkancavdar.qrmenu.auth.adapter.persistence.repository;

import com.furkancavdar.qrmenu.auth.adapter.persistence.mapper.RoleEntityMapper;
import com.furkancavdar.qrmenu.auth.adapter.persistence.repository.JpaRoleRepository;
import com.furkancavdar.qrmenu.auth.application.port.out.RoleRepositoryPort;
import com.furkancavdar.qrmenu.auth.domain.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class RoleAdapter implements RoleRepositoryPort {

    private final JpaRoleRepository jpaRoleRepository;

    @Override
    public Optional<Role> findByName(String name) {
        return jpaRoleRepository.findByName(name)
                .map(RoleEntityMapper::toDomain);
    }
}