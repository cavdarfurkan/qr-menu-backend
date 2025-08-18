package com.furkancavdar.qrmenu.auth.config;

import com.furkancavdar.qrmenu.auth.domain.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.stream.Collectors;

public class CustomUserDetails extends User implements UserDetails {

    public CustomUserDetails(String username, String password, String email) {
        super(username, password, email);
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return super.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.name()))
                .collect(Collectors.toList());
    }

//    @Override
//    public String getPassword() {
//        return super.getPassword();
//    }
//
//    @Override
//    public String getUsername() {
//        return super.getUsername();
//    }

    @Override
    public boolean isAccountNonExpired() {
        return super.isAccountNonExpired();
    }

    @Override
    public boolean isAccountNonLocked() {
        return super.isAccountNonLocked();
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return super.isCredentialsNonExpired();
    }

    @Override
    public boolean isEnabled() {
        return super.isEnabled();
    }
}
