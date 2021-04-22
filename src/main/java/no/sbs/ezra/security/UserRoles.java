package no.sbs.ezra.security;

import com.google.common.collect.Sets;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Set;
import java.util.stream.Collectors;


public enum UserRoles {
    MASTER(Sets.newHashSet(UserPermission.MASTER, UserPermission.ADMIN, UserPermission.MEMBER, UserPermission.FOLLOWER, UserPermission.VISITOR)),
    ADMIN(Sets.newHashSet(UserPermission.ADMIN, UserPermission.MEMBER, UserPermission.FOLLOWER, UserPermission.VISITOR)),
    MEMBER(Sets.newHashSet(UserPermission.MEMBER, UserPermission.FOLLOWER, UserPermission.VISITOR)),
    FOLLOWER(Sets.newHashSet(UserPermission.FOLLOWER, UserPermission.VISITOR)),
    VISITOR(Sets.newHashSet(UserPermission.VISITOR));

    private final Set<UserPermission> permission;


    UserRoles(Set<UserPermission> permission) {
        this.permission = permission;
    }

    public Set<UserPermission> getPermission() {
        return permission;
    }

    public Set<SimpleGrantedAuthority> getGrantedAuthority() {
        Set<SimpleGrantedAuthority> permissions = getPermission().stream()
                .map(permission -> new SimpleGrantedAuthority(permission.getPermission()))
                .collect(Collectors.toSet());
        permissions.add(new SimpleGrantedAuthority("ROLE_" + this.name()));
        return permissions;
    }
}
