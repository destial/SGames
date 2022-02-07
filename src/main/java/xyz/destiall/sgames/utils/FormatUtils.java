package xyz.destiall.sgames.utils;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import xyz.destiall.sgames.SGames;
import xyz.destiall.sgames.config.ConfigKey;
import xyz.destiall.sgames.lobby.Lobby;
import xyz.destiall.sgames.manager.MatchManager;
import xyz.destiall.sgames.map.MapInfo;
import xyz.destiall.sgames.match.Match;
import xyz.destiall.sgames.player.Competitor;
import xyz.destiall.sgames.player.Spectator;
import xyz.destiall.sgames.player.modules.SpectateModule;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class FormatUtils {
    private FormatUtils() {}

    public static String formatMatch(String line, Match match) {
        if (match.isFinishing()) {
            Competitor winner = match.getWinner();
            line = line.replace("{winner}", winner != null ? winner.getName() : "Unknown");
        }
        return line.replace("{alive}", "" + match.getAlive())
                .replace("{spectating}", "" + match.getSpectating())
                .replace("{time}", match.getCountdown().getFormattedDuration())
                .replace("{map}", match.getMap().getInfo().getName());
    }

    public static String formatPlayer(String line, Player player) {
        Location location = player.getLocation();
        return SGames.INSTANCE.getConfigManager().papi(line.replace("{player}", player.getName())
                .replace("{health}", "" + player.getHealth())
                .replace("{hunger}", "" + player.getFoodLevel())
                .replace("{x}", "" + location.getBlockX())
                .replace("{y}", "" + location.getBlockY())
                .replace("{z}", "" + location.getBlockZ())
                .replace("{direction}", "" + getCardinal(location)), player);
    }

    public static String formatMatchPlayer(String line, Player player, Match match) {
        Player p = player;
        SpectateModule m = match.getModule(SpectateModule.class);
        if (m != null && m.isSpectating(player.getUniqueId())) {
            Spectator spectator = match.getSpectator(player.getUniqueId());
            if (spectator != null && spectator.getBukkit().isPresent()) {
                p = spectator.getBukkit().get();
            }
        }
        return formatMatch(formatPlayer(line, p), match);
    }

    public static String formatLobby(String line, Lobby lobby) {
        MatchManager mm = SGames.INSTANCE.getMatchManager();
        line = line.replace("{queue}", "" + lobby.queue().size())
                .replace("{time}", lobby.getCountdown().getFormattedDuration())
                .replace("{max}", ""+SGames.INSTANCE.getConfigManager().getInt(ConfigKey.MAX_PLAYERS))
                .replace("{min}", ""+SGames.INSTANCE.getConfigManager().getInt(ConfigKey.MIN_PLAYERS));

        Set<Map.Entry<MapInfo, Set<UUID>>> votingMaps = mm.getVoteManager().getVotingMaps();
        int i = 1;
        for (Map.Entry<MapInfo, Set<UUID>> entry : votingMaps) {
            line = line.replace("{map" + i + "}", entry.getKey().getName()).replace("{votes" + i + "}", ""+entry.getValue().size());
            i++;
        }
        return line;
    }

    public static String formatLobbyPlayer(String line, Player player, Lobby lobby) {
        return formatLobby(formatPlayer(line, player), lobby);
    }

    public static String getCardinal(Location location) {
        double rotation = (location.getYaw() - 90) % 360;
        if (rotation < 0) {
            rotation += 360.0;
        }
        if (0 <= rotation && rotation < 22.5) {
            return "N";
        } else if (22.5 <= rotation && rotation < 67.5) {
            return "NE";
        } else if (67.5 <= rotation && rotation < 112.5) {
            return "E";
        } else if (112.5 <= rotation && rotation < 157.5) {
            return "SE";
        } else if (157.5 <= rotation && rotation < 202.5) {
            return "S";
        } else if (202.5 <= rotation && rotation < 247.5) {
            return "SW";
        } else if (247.5 <= rotation && rotation < 292.5) {
            return "W";
        } else if (292.5 <= rotation && rotation < 337.5) {
            return "NW";
        } else if (337.5 <= rotation && rotation < 360.0) {
            return "N";
        }
        return "Unknown";
    }
}
