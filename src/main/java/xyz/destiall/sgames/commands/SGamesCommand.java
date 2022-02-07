package xyz.destiall.sgames.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import xyz.destiall.sgames.SGames;
import xyz.destiall.sgames.commands.admin.LobbySetupCommand;
import xyz.destiall.sgames.commands.admin.MapCancelCommand;
import xyz.destiall.sgames.commands.admin.MapFinishCommand;
import xyz.destiall.sgames.commands.admin.MapSpawnPointCommand;
import xyz.destiall.sgames.commands.admin.MapStartCommand;
import xyz.destiall.sgames.commands.admin.MatchStartCommand;
import xyz.destiall.sgames.commands.player.VoteCommand;
import xyz.destiall.sgames.config.ConfigKey;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class SGamesCommand implements CommandExecutor, TabExecutor {
    private final List<BaseCommand> commands;

    public SGamesCommand(SGames plugin) {
        commands = new ArrayList<>();
        commands.add(new VoteCommand());
        commands.add(new MatchStartCommand());

        if (plugin.getConfigManager().getBoolean(ConfigKey.SETUP_MODE)) {
            commands.add(new LobbySetupCommand());
            commands.add(new MapStartCommand());
            commands.add(new MapSpawnPointCommand());
            commands.add(new MapFinishCommand());
            commands.add(new MapCancelCommand());
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(ChatColor.RED + "Invalid arguments!");
            return false;
        }
        BaseCommand baseCommand = commands.stream().filter(c -> c.subCommand.equalsIgnoreCase(args[0])).findFirst().orElse(null);
        if (baseCommand == null) {
            sender.sendMessage(ChatColor.RED + "Invalid arguments!");
            return false;
        }
        if (baseCommand.permission != null && !sender.hasPermission(baseCommand.permission)) {
            sender.sendMessage(ChatColor.RED + "You do not have permission!");
            return false;
        }
        baseCommand.execute(sender, Arrays.copyOfRange(args, 1, args.length));
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String s, String[] args) {
        return commands.stream().filter(b -> b.permission == null || sender.hasPermission(b.permission)).map(BaseCommand::getSubCommand).collect(Collectors.toList());
    }
}
