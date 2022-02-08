package xyz.destiall.sgames.commands.setup;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import xyz.destiall.sgames.commands.BaseCommand;
import xyz.destiall.sgames.session.CreationSession;

public class MapFinishCommand extends BaseCommand {
    public MapFinishCommand() {
        super("finishmap", "sgames.createmap");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            if (!CreationSession.SESSIONS.containsKey(player.getUniqueId())) {
                player.sendMessage(ChatColor.RED + "You are currently not in a map creation session!");
                player.sendMessage(ChatColor.RED + "To start a session: /sg createmap [name]");
                return;
            }
            CreationSession session = CreationSession.of(player);
            try {
                if (session.save()) {
                    player.sendMessage(ChatColor.GREEN + "You have successfully saved " + session.getName());
                }
            } catch (Exception e) {
                e.printStackTrace();
                player.sendMessage(ChatColor.RED + e.getMessage());
            }
        }
    }
}
