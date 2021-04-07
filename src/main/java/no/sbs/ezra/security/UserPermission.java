package no.sbs.ezra.security;

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
