package xyz.destiall.sgames.api;

import org.bukkit.Bukkit;
import org.bukkit.permissions.Permission;

public interface Permissions {
    static Permission register(String permission) {
        Permission perms = new Permission(permission);
        try {
            Bukkit.getPluginManager().addPermission(perms);
        } catch (Exception ignored) {}
        return perms;
    }
}
