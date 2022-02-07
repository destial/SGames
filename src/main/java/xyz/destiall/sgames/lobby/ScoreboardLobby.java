package xyz.destiall.sgames.lobby;

import fr.mrmicky.fastboard.FastBoard;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import xyz.destiall.sgames.SGames;
import xyz.destiall.sgames.api.Module;
import xyz.destiall.sgames.api.Tickable;
import xyz.destiall.sgames.manager.ConfigManager;
import xyz.destiall.sgames.utils.FormatUtils;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ScoreboardLobby implements Module, Tickable {
    private final ConcurrentHashMap<UUID, FastBoard> boards;
    private final Lobby lobby;
    private final String title;
    private final String[] lines;

    public ScoreboardLobby(Lobby lobby) {
        this.lobby = lobby;
        this.boards = new ConcurrentHashMap<>();
        File scoreboardYml = new File(SGames.INSTANCE.getDataFolder(), "scoreboard.yml");
        if (!scoreboardYml.exists()) {
            SGames.INSTANCE.saveResource("scoreboard.yml", false);
        }
        YamlConfiguration scoreboardConfig = YamlConfiguration.loadConfiguration(scoreboardYml);
        title = ChatColor.translateAlternateColorCodes('&', scoreboardConfig.getString("lobby.title", "Lobby"));
        List<String> lines = scoreboardConfig.getStringList("lobby.lines");
        for (int i = 0; i < lines.size(); i++) {
            String l = ChatColor.translateAlternateColorCodes('&', lines.get(i));
            lines.set(i, l);
        }
        String footer = scoreboardConfig.getString("lobby.footer", "&eplay.sgcraft.net");
        if (lines.size() < 16) {
            lines.add(ChatColor.translateAlternateColorCodes('&', footer));
        }
        this.lines = lines.toArray(new String[]{});
    }

    public void addPlayer(Player player) {
        removePlayer(player);
        boards.put(player.getUniqueId(), new FastBoard(player));
    }

    public void removePlayer(Player player) {
        if (boards.containsKey(player.getUniqueId()))
            boards.remove(player.getUniqueId()).delete();
    }

    @Override
    public boolean isLoaded() {
        return title != null;
    }

    @Override
    public void tick() {
        String[] lines = Arrays.copyOf(this.lines, this.lines.length);
        ConfigManager configManager = SGames.INSTANCE.getConfigManager();
        for (FastBoard board : boards.values()) {
            String title = FormatUtils.formatLobbyPlayer(this.title, board.getPlayer(), lobby);
            if (configManager.PAPI) {
                title = PlaceholderAPI.setPlaceholders(board.getPlayer(), title);
            }

            for (int i = 0; i < lines.length; i++) {
                String line = lines[i];
                line = FormatUtils.formatLobbyPlayer(line, board.getPlayer(), lobby);

                if (configManager.PAPI) {
                    line = PlaceholderAPI.setPlaceholders(board.getPlayer(), line);
                }
                lines[i] = line;
            }

            board.updateTitle(title);
            board.updateLines(lines);
        }
    }

    @Override
    public void unload() {
        for (FastBoard board : boards.values()) {
            board.delete();
        }
        boards.clear();
    }
}
