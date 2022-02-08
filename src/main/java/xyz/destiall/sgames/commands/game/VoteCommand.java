package xyz.destiall.sgames.commands.game;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import xyz.destiall.sgames.commands.BaseCommand;
import xyz.destiall.sgames.map.MapInfo;

import java.util.Optional;

public class VoteCommand extends BaseCommand {
    public VoteCommand() {
        super("vote");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            if (plugin.getMatchManager().getMatch() != null) {
                player.sendMessage(ChatColor.RED + "You may not vote at this time!");
                return;
            }
            if (args.length == 0) {
                plugin.getMatchManager().getVoteManager().openBook(player, true);
                return;
            }
            Optional<MapInfo> mapInfo = plugin.getMapManager().getMapInfo(String.join(" ", args));
            if (!mapInfo.isPresent()) {
                player.sendMessage(ChatColor.RED + "Invalid map name!");
                return;
            }
            if (!plugin.getMatchManager().getVoteManager().toggleVote(player.getUniqueId(), mapInfo.get())) {
                player.sendMessage(ChatColor.RED + "This map is not part of the vote!");
                return;
            }
            plugin.getMatchManager().getVoteManager().openBook(player, true);
            player.sendMessage(ChatColor.GREEN + "Toggled vote for " + mapInfo.get().getName());
        }
    }
}
