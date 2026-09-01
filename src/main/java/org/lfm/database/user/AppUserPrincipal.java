package org.lfm.database.user;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

public class AppUserPrincipal implements UserDetails
{
    private final Long id;
    private final String username;
    private final String passwordHash;
    private final String fullName;
    private final Role role;
    private final boolean enabled;

    public AppUserPrincipal(AppUser user)
    {
        this.id = user.getId();
        this.username = user.getUsername();
        this.passwordHash = user.getPasswordHash();
        this.fullName = user.getFullName();
        this.role = user.getRole();
        this.enabled = user.isEnabled();
    }

    public Long getId()
    {
        return id;
    }

    public String getFullName()
    {
        return fullName;
    }

    public Role getRole()
    {
        return role;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities()
    {
        return List.of(new SimpleGrantedAuthority(role.authority()));
    }

    @Override
    public String getPassword()
    {
        return passwordHash;
    }

    @Override
    public String getUsername()
    {
        return username;
    }

    @Override
    public boolean isAccountNonExpired()
    {
        return true;
    }

    @Override
    public boolean isAccountNonLocked()
    {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired()
    {
        return true;
    }

    @Override
    public boolean isEnabled()
    {
        return enabled;
    }
}
