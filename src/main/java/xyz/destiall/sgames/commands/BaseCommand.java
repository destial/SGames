package xyz.destiall.sgames.commands;

import org.bukkit.command.CommandSender;
import xyz.destiall.sgames.SGames;
import xyz.destiall.sgames.api.Permissions;

public abstract class BaseCommand {
    protected final String subCommand;
    protected final SGames plugin;
    protected String permission;

    public BaseCommand(String subCommand, String permission) {
        this.subCommand = subCommand;
        this.permission = permission;
        this.plugin = SGames.INSTANCE;
        Permissions.register(permission);
    }

    public BaseCommand(String subCommand) {
        this.subCommand = subCommand;
        this.plugin = SGames.INSTANCE;
    }

    public String getSubCommand() {
        return subCommand;
    }

    public String getPermission() {
        return permission;
    }

    public abstract void execute(CommandSender sender, String[] args);
}
