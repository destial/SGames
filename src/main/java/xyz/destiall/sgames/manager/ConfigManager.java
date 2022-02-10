package xyz.destiall.sgames.manager;

import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import xyz.destiall.sgames.SGames;
import xyz.destiall.sgames.config.ConfigKey;
import xyz.destiall.sgames.config.MessageKey;
import xyz.destiall.sgames.config.simpleconfig.SimpleConfig;
import xyz.destiall.sgames.config.simpleconfig.SimpleConfigManager;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;

public class ConfigManager {
    private final SGames plugin;
    private final boolean PAPI;
    private SimpleConfigManager scm;
    private SimpleConfig configuration;

    public ConfigManager(SGames plugin) {
        this.plugin = plugin;
        scm = new SimpleConfigManager(plugin);
        configuration = scm.getNewConfig("config.yml", null, true);
        PAPI = plugin.getServer().getPluginManager().getPlugin("PlaceholderAPI") != null;
    }

    public void reload() {
        plugin.reloadConfig();
        scm = new SimpleConfigManager(plugin);
        configuration = scm.getNewConfig("config.yml", null, true);
    }

    public boolean getBoolean(ConfigKey key) {
        if (!configuration.contains(key.key)) {
            noKey(key);
        }
        return configuration.getBoolean(key.key, (boolean) key.def);
    }

    public String getString(ConfigKey key) {
        if (!configuration.contains(key.key)) {
            noKey(key);
        }
        return configuration.getString(key.key, (String) key.def);
    }

    public String getMessage(MessageKey key) {
        return ChatColor.translateAlternateColorCodes('&', configuration.getString(key.key, key.def));
    }

    public int getInt(ConfigKey key) {
        if (!configuration.contains(key.key)) {
            noKey(key);
        }
        return configuration.getInt(key.key, (int) key.def);
    }

    public double getDouble(ConfigKey key) {
        if (!configuration.contains(key.key)) {
            noKey(key);
        }
        return configuration.getDouble(key.key, (double) key.def);
    }

    public float getFloat(ConfigKey key) {
        if (!configuration.contains(key.key)) {
            noKey(key);
        }
        return (float) getDouble(key);
    }

    public void noKey(ConfigKey key) {
        SGames.INSTANCE.getLogger().warning("Configuration key " + key.key + " not found! Using default: " + key.def);
        configuration.set(key.key, key.def);
        save();
    }

    public void noKey(MessageKey key) {
        SGames.INSTANCE.getLogger().warning("Message key " + key.key + " not found! Using default: " + key.def);
        configuration.set(key.key, key.def);
        save();
    }

    public void save() {
        configuration.saveConfig();
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
