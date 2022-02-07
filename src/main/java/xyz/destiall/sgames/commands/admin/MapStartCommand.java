package xyz.destiall.sgames.commands.admin;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import xyz.destiall.sgames.commands.BaseCommand;
import xyz.destiall.sgames.session.CreationSession;

public class MapStartCommand extends BaseCommand {
    public MapStartCommand() {
        super("createmap", "sgames.createmap");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            if (args.length == 0) {
                player.sendMessage(ChatColor.RED + "You need to provide a name for this map!");
                return;
            }
            if (CreationSession.SESSIONS.containsKey(player.getUniqueId())) {
                CreationSession session = CreationSession.of(player);
                if (session.getWorld() != player.getWorld()) {
                    player.sendMessage(ChatColor.RED + "You are in the wrong world!");
                    player.sendMessage(ChatColor.RED + "To stop map creation: /sg cancelmap");
                    player.sendMessage(ChatColor.RED + "To save the map: /sg finishmap");
                    return;
                }
            }
            CreationSession session = CreationSession.of(player);
            String name = String.join(" ", args);
            session.setName(name);
            player.sendMessage(ChatColor.GREEN + "You have set the map name to be " + name);
            player.sendMessage(ChatColor.GREEN + "You can add spawnpoints by: /sg addspawn");
        }
    }
}
