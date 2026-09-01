package org.lfm.database.user;

public enum Role
{
    VIEWER,
    STAFF,
    ADMIN;

    public String authority()
    {
        return "ROLE_" + name();
    }
}
