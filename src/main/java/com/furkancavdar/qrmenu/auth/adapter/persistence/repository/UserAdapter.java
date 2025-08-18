package com.furkancavdar.qrmenu.auth.adapter.persistence.repository;

import com.furkancavdar.qrmenu.auth.adapter.persistence.mapper.UserEntityMapper;
import com.furkancavdar.qrmenu.auth.adapter.persistence.repository.JpaUserRepository;
import com.furkancavdar.qrmenu.auth.application.port.out.UserRepositoryPort;
import com.furkancavdar.qrmenu.auth.domain.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Optional;

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
    public Optional<User> findByUsername(String username) {
        log.info("UserAdapter:findByUsername caller: {}.{}", Thread.currentThread().getStackTrace()[2].getClassName(), Thread.currentThread().getStackTrace()[2].getMethodName());
        return jpaUserRepository.findByUsername(username).map(UserEntityMapper::toDomain);
    }

    @Override
    public boolean existsByUsername(String username) {
        return jpaUserRepository.existsByUsername(username);
    }

    @Override
    public boolean existsByEmail(String email) {
        return jpaUserRepository.existsByEmail(email);
    }
} 