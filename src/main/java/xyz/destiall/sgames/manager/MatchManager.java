package xyz.destiall.sgames.manager;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldInitEvent;
import xyz.destiall.sgames.SGames;
import xyz.destiall.sgames.api.Module;
import xyz.destiall.sgames.lobby.Lobby;
import xyz.destiall.sgames.map.MapInfo;
import xyz.destiall.sgames.match.Match;
import xyz.destiall.sgames.match.MatchFactory;
import xyz.destiall.sgames.match.MatchLoader;
import xyz.destiall.sgames.match.events.MatchLoadEvent;
import xyz.destiall.sgames.utils.FileUtils;

import java.io.File;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

public class MatchManager implements Module, Listener {
    private final SGames plugin;
    private final VoteManager voteManager;
    private final Lobby lobby;
    private Match match;
    private File lobbyFile;

    public MatchManager(SGames plugin) {
        this.plugin = plugin;
        this.voteManager = new VoteManager(plugin);
        this.lobby = new Lobby(null);
        try {
            lobbyFile = new File(plugin.getDataFolder(), "lobby.yml");
            if (!lobbyFile.exists()) lobbyFile.createNewFile();

            File scoreboardYml = new File(plugin.getDataFolder(), "scoreboard.yml");
            if (!scoreboardYml.exists()) plugin.saveResource("scoreboard.yml", false);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public MatchLoader createMatch() {
        if (match == null) {
            MapInfo mostVoted = voteManager.getVotedMap();
            if (mostVoted == null) return null;
            voteManager.unload();
            return new MatchLoader(new MatchFactory(lobby, mostVoted));
        }
        return null;
    }

    public void callEvent(Event event) {
        Bukkit.getPluginManager().callEvent(event);
    }

    public Future<MatchLoader> loadMatchAsync() {
        return runAsyncThread(this::createMatch);
    }

    public Future<MatchLoader> loadMatchSync() {
        return runMainThread(this::createMatch);
    }

    public Match getMatch() {
        return match;
    }

    @Override
    public void load() {
        try {
            YamlConfiguration lobbyConfig = YamlConfiguration.loadConfiguration(lobbyFile);
            World lobbyWorld = Bukkit.getWorld(lobbyConfig.getString("world", "world"));
            Location location = new Location(lobbyWorld, lobbyConfig.getDouble("x"), lobbyConfig.getDouble("y"), lobbyConfig.getDouble("z"), (float) lobbyConfig.getDouble("yaw"), (float) lobbyConfig.getDouble("pitch"));
            lobby.setPoint(location);
        } catch (Exception e) {
            e.printStackTrace();
        }
        voteManager.load();

        try {
            File destination = new File(Bukkit.getWorldContainer(), "match");
            if (destination.exists()) {
                FileUtils.delete(destination);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    public VoteManager getVoteManager() {
        return voteManager;
    }

    @Override
    public void unload() {
        HandlerList.unregisterAll(this);
        voteManager.unload();
        if (match != null) {
            match.unload();
            World existingWorld = plugin.getServer().getWorld("match");
            if (existingWorld != null) {
                plugin.getServer().unloadWorld(existingWorld, false);
            }
            FileUtils.delete(new File(Bukkit.getWorldContainer(), "match"));
        }
        match = null;
    }

    @Override
    public boolean isLoaded() {
        return voteManager.isLoaded();
    }

    public Lobby getLobby() {
        return lobby;
    }

    private <V> Future<V> runMainThread(Callable<V> task) {
        return plugin.getServer().getScheduler().callSyncMethod(plugin, task);
    }

    private <V> CompletableFuture<V> runAsyncThread(Callable<V> task) {
        return CompletableFuture.supplyAsync(
                () -> {
                    try {
                        return task.call();
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onMatchLoad(MatchLoadEvent e) {
        SGames.INSTANCE.getLogger().info("Loading match world " + e.getMatch().getMap().getInfo().getName());
        match = e.getMatch();
        match.addCompetitors(lobby.queue());
        match.start();
        lobby.unload();
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onWorldInit(WorldInitEvent e) {
        SGames.INSTANCE.getLogger().info("Initializing world " + e.getWorld());
        e.getWorld().setKeepSpawnInMemory(false);
    }

    public File getLobbyYml() {
        return lobbyFile;
    }
}
