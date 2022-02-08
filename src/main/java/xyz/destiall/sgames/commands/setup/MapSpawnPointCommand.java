package xyz.destiall.sgames.commands.setup;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import xyz.destiall.sgames.commands.BaseCommand;
import xyz.destiall.sgames.session.CreationSession;

public class MapSpawnPointCommand extends BaseCommand {
    public MapSpawnPointCommand() {
        super("addspawn", "sgames.createmap");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            if (CreationSession.SESSIONS.containsKey(player.getUniqueId())) {
                CreationSession session = CreationSession.of(player);
                if (session.getWorld() != player.getWorld()) {
                    player.sendMessage(ChatColor.RED + "You are in the wrong world!");
                    player.sendMessage(ChatColor.RED + "To stop map creation: /sg cancelmap");
                    player.sendMessage(ChatColor.RED + "To save the map: /sg finishmap");
                    return;
                }
            }
            Location location = player.getLocation();
            CreationSession session = CreationSession.of(player);
            session.getSpawnPoints().add(location);
            player.sendMessage(ChatColor.GREEN + "Added spawnpoint " + session.getSpawnPoints().size());
            if (session.getName() == null) player.sendMessage(ChatColor.GREEN + "You can set the map name by: /sg createmap [name]");
        }
    }
}
