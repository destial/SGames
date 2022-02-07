package xyz.destiall.sgames;

import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;
import xyz.destiall.sgames.commands.SGamesCommand;
import xyz.destiall.sgames.config.ConfigKey;
import xyz.destiall.sgames.datastore.Datastore;
import xyz.destiall.sgames.listeners.BukkitListener;
import xyz.destiall.sgames.listeners.MatchListener;
import xyz.destiall.sgames.manager.ChestManager;
import xyz.destiall.sgames.manager.ConfigManager;
import xyz.destiall.sgames.manager.MapManager;
import xyz.destiall.sgames.manager.MatchManager;
import xyz.destiall.sgames.utils.SchedulerUtils;

public final class SGames extends JavaPlugin {
    public static SGames INSTANCE;

    private MapManager mapManager;
    private ConfigManager configManager;
    private MatchManager matchManager;
    private ChestManager chestManager;
    private Datastore datastore;
    private SchedulerUtils schedulerUtils;
    private BukkitAudiences pluginAudience;

    @Override
    public void onEnable() {
        INSTANCE = this;
        saveDefaultConfig();

        schedulerUtils = new SchedulerUtils(this);
        pluginAudience = BukkitAudiences.create(this);

        configManager = new ConfigManager(this);
        chestManager = new ChestManager(this);
        mapManager = new MapManager(this);
        matchManager = new MatchManager(this);

        datastore = new Datastore(this);

        if (!configManager.getBoolean(ConfigKey.SETUP_MODE)) {
            getServer().getPluginManager().registerEvents(new BukkitListener(this), this);
            mapManager.load();
            getLogger().info("Loaded MapManager");
            chestManager.load();
            getLogger().info("Loaded ChestManager");
            matchManager.load();
            getLogger().info("Loaded MatchManager");
            datastore.load();
            getLogger().info("Loaded Datastore");
        }

        getServer().getPluginCommand("sgames").setExecutor(new SGamesCommand(this));
        getServer().getPluginManager().registerEvents(new MatchListener(this), this);

    }

    @Override
    public void onDisable() {
        chestManager.unload();
        matchManager.unload();
        mapManager.unload();
        datastore.unload();

        HandlerList.unregisterAll(this);
    }

    public Datastore getDatastore() {
        return datastore;
    }

    public MapManager getMapManager() {
        return mapManager;
    }

    public MatchManager getMatchManager() {
        return matchManager;
    }

    public BukkitAudiences getPluginAudience() {
        return pluginAudience;
    }

    public ChestManager getChestManager() {
        return chestManager;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public SchedulerUtils getScheduler() {
        return schedulerUtils;
    }
}
