package xyz.destiall.sgames.manager;

import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import xyz.destiall.sgames.SGames;
import xyz.destiall.sgames.config.ConfigKey;
import xyz.destiall.sgames.config.MessageKey;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;

public class ConfigManager {
    private final SGames plugin;
    private FileConfiguration configuration;
    public boolean PAPI;

    public ConfigManager(SGames plugin) {
        this.plugin = plugin;
        configuration = plugin.getConfig();
        PAPI = plugin.getServer().getPluginManager().getPlugin("PlaceholderAPI") != null;
    }

    public void reload() {
        plugin.reloadConfig();
        configuration = plugin.getConfig();
    }

    public boolean getBoolean(ConfigKey key) {
        return configuration.getBoolean(key.key, (boolean) key.def);
    }

    public String getString(ConfigKey key) {
        return configuration.getString(key.key, (String) key.def);
    }

    public String getMessage(MessageKey key) {
        return ChatColor.translateAlternateColorCodes('&', configuration.getString(key.key, key.def));
    }

    public int getInt(ConfigKey key) {
        return configuration.getInt(key.key, (int) key.def);
    }

    public double getDouble(ConfigKey key) {
        return configuration.getDouble(key.key, (double) key.def);
    }

    public float getFloat(ConfigKey key) {
        return (float) getDouble(key);
    }

    public Duration getDuration(ConfigKey key) {
        String string = getString(key).trim().toLowerCase();
        TemporalUnit unit = ChronoUnit.SECONDS;
        if (string.endsWith("ms")) {
            unit = ChronoUnit.MILLIS;
            string = string.substring(0, string.length() - 2);
        } else if (string.endsWith("m")) {
            unit = ChronoUnit.MINUTES;
            string = string.substring(0, string.length() - 1);
        } else if (string.endsWith("h")) {
            unit = ChronoUnit.HOURS;
            string = string.substring(0, string.length() - 1);
        } else if (string.endsWith("s")) {
            string = string.substring(0, string.length() - 1);
        }
        int i = 1;
        try {
            i = Integer.parseInt(string);
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        return Duration.of(i, unit);
    }

    public String papi(String line, Player player) {
        if (PAPI) {
            return PlaceholderAPI.setPlaceholders(player, line);
        }
        return line;
    }
}
