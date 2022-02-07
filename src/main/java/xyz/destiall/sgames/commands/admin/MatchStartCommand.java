package xyz.destiall.sgames.commands.admin;

import org.bukkit.command.CommandSender;
import xyz.destiall.sgames.commands.BaseCommand;
import xyz.destiall.sgames.lobby.Lobby;

public class MatchStartCommand extends BaseCommand {
    public MatchStartCommand() {
        super("start", "sgames.start");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        Lobby lobby = plugin.getMatchManager().getLobby();
        lobby.load();
    }
}
