package xyz.destiall.sgames.commands.admin;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import xyz.destiall.sgames.commands.BaseCommand;

import java.io.File;

public class LobbySetupCommand extends BaseCommand {
    public LobbySetupCommand() {
        super("setlobby", "sgames.setlobby");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            Location location = player.getLocation();
            try {
                File lobbyFile = plugin.getMatchManager().getLobbyYml();
                YamlConfiguration configuration = YamlConfiguration.loadConfiguration(lobbyFile);
                configuration.set("world", player.getWorld().getName());
                configuration.set("x", location.getX());
                configuration.set("y", location.getY());
                configuration.set("z", location.getZ());
                configuration.set("yaw", location.getYaw());
                configuration.set("pitch", location.getPitch());
                configuration.save(lobbyFile);
                player.sendMessage(ChatColor.GREEN + "You have set the lobby!");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
