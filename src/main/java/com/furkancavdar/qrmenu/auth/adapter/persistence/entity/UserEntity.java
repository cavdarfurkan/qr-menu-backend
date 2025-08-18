package com.furkancavdar.qrmenu.auth.adapter.persistence.entity;

import com.furkancavdar.qrmenu.menu_module.adapter.persistence.entity.MenuEntity;
import com.furkancavdar.qrmenu.theme_module.adapter.persistence.entity.ThemeEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String email;

    private boolean enabled = true;
    private boolean accountNonExpired = true;
    private boolean accountNonLocked = true;
    private boolean credentialsNonExpired = true;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<RoleEntity> roles = new HashSet<>();

    @OneToMany(mappedBy = "owner", orphanRemoval = true)
    private Set<ThemeEntity> themes = new LinkedHashSet<>();

    @OneToMany(mappedBy = "owner", orphanRemoval = true)
    private Set<MenuEntity> menus = new LinkedHashSet<>();

}