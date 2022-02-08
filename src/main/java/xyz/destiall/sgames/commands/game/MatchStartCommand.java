package xyz.destiall.sgames.commands.game;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import xyz.destiall.sgames.commands.BaseCommand;
import xyz.destiall.sgames.lobby.Lobby;

public class MatchStartCommand extends BaseCommand {
    public MatchStartCommand() {
        super("start", "sgames.start");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (plugin.getMatchManager().getMatch() != null) {
            sender.sendMessage(ChatColor.RED + "The match has already started!");
            return;
        }
        Lobby lobby = plugin.getMatchManager().getLobby();
        if (lobby.isLoaded()) {
            sender.sendMessage(ChatColor.RED + "The game is already about to start!");
            return;
        }
        lobby.load();
    }
}
