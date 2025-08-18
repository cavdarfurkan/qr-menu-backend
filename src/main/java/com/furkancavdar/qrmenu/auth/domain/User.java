package com.furkancavdar.qrmenu.auth.domain;

import lombok.Getter;
import lombok.ToString;

import java.util.HashSet;
import java.util.Set;

@Getter
@ToString
public class User {
    private Long id;
    private final String username;
    private final String password;
    private final String email;

    private final boolean enabled = true;
    private final boolean accountNonExpired = true;
    private final boolean accountNonLocked = true;
    private final boolean credentialsNonExpired = true;

    private final Set<Role> roles = new HashSet<>();

    public User(String username, String password, String email) {
        this.username = username;
        this.password = password;
        this.email = email;
    }

    public User(Long id, String username, String password, String email) {
        this(username, password, email);
        this.id = id;
    }

    public void addRole(Role role) {
        if (roles.contains(role)) {
            return;
        }
        roles.add(role);
    }

    public void removeRole(Role role) {
        if (!roles.contains(role)) {
            return;
        }
        roles.remove(role);
    }

}