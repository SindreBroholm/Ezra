package no.sbs.ezra.security;

import java.util.ArrayList;
import java.util.List;

public enum UserPermission {
     MASTER("master"),
    ADMIN("admin"),
    MEMBER("member"),
    FOLLOWER("follower"),
    VISITOR("visitor");

    private final String permission;

    UserPermission(String permission){
        this.permission = permission;
    }

    public String getPermission() {
        return permission;
    }

}
