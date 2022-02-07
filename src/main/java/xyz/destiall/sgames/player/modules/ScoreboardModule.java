package xyz.destiall.sgames.player.modules;

import fr.mrmicky.fastboard.FastBoard;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import xyz.destiall.sgames.SGames;
import xyz.destiall.sgames.api.Module;
import xyz.destiall.sgames.api.Tickable;
import xyz.destiall.sgames.countdown.Countdown;
import xyz.destiall.sgames.manager.ConfigManager;
import xyz.destiall.sgames.match.Match;
import xyz.destiall.sgames.match.events.MatchMoveEvent;
import xyz.destiall.sgames.utils.FormatUtils;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ScoreboardModule implements Module, Tickable, Listener {
    private final ConcurrentHashMap<UUID, FastBoard> boards;
    private final Match match;
    private Countdown countdown;
    private String title;
    private String[] lines;

    public ScoreboardModule(Match match) {
        this.match = match;
        boards = new ConcurrentHashMap<>();
    }

    public Match getMatch() {
        return match;
    }

    @Override
    public void load() {
        File scoreboardYml = new File(SGames.INSTANCE.getDataFolder(), "scoreboard.yml");
        if (!scoreboardYml.exists()) {
            SGames.INSTANCE.saveResource("scoreboard.yml", false);
        }
        YamlConfiguration scoreboardConfig = YamlConfiguration.loadConfiguration(scoreboardYml);
        title = ChatColor.translateAlternateColorCodes('&', scoreboardConfig.getString("game.title", "{map}"));
        List<String> lines = scoreboardConfig.getStringList("game.lines");
        for (int i = 0; i < lines.size(); i++) {
            String l = ChatColor.translateAlternateColorCodes('&', lines.get(i));
            lines.set(i, l);
        }
        String footer = scoreboardConfig.getString("game.footer", "&eplay.sgcraft.net");
        if (lines.size() < 16) {
            lines.add(ChatColor.translateAlternateColorCodes('&', footer));
        }
        this.lines = lines.toArray(new String[]{});
    }

    public void addPlayer(Player player) {
        if (boards.containsKey(player.getUniqueId())) {
            boards.get(player.getUniqueId()).delete();
        }
        boards.put(player.getUniqueId(), new FastBoard(player));
    }

    @Override
    public void unload() {
        for (FastBoard board : boards.values()) {
            board.delete();
        }
        boards.clear();
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
            String title = FormatUtils.formatMatchPlayer(this.title, board.getPlayer(), match);
            if (configManager.PAPI) {
                title = PlaceholderAPI.setPlaceholders(board.getPlayer(), title);
            }

            for (int i = 0; i < lines.length; i++) {
                String line = lines[i];
                line = FormatUtils.formatMatchPlayer(line, board.getPlayer(), match);

                if (configManager.PAPI) {
                    line = PlaceholderAPI.setPlaceholders(board.getPlayer(), line);
                }
                lines[i] = line;
            }

            board.updateTitle(title);
            board.updateLines(lines);
        }
    }

    @EventHandler
    public void onMatchMove(MatchMoveEvent e) {
        e.getMatch().forEachPlayer(p -> addPlayer(p.getBukkit().get()));
    }
}
