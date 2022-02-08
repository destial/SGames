package xyz.destiall.sgames.lobby;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;
import xyz.destiall.sgames.SGames;
import xyz.destiall.sgames.api.Module;
import xyz.destiall.sgames.api.Tickable;
import xyz.destiall.sgames.config.ConfigKey;
import xyz.destiall.sgames.countdown.Countdown;
import xyz.destiall.sgames.countdown.events.CountdownEndEvent;
import xyz.destiall.sgames.lobby.events.CancelledStartEvent;
import xyz.destiall.sgames.lobby.events.QueueRemoveEvent;
import xyz.destiall.sgames.lobby.events.ReadyToStartEvent;
import xyz.destiall.sgames.match.events.MatchInitEvent;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

public class Lobby implements Module, Listener, Tickable {
    private final ConcurrentHashMap<Player, Location> queue;
    private final Countdown countdown;
    private final ScoreboardLobby scoreboardLobby;
    private BukkitTask task;
    private Location spawnPoint;

    public Lobby(Location location) {
        this.queue = new ConcurrentHashMap<>();
        this.spawnPoint = location;
        this.scoreboardLobby = new ScoreboardLobby(this);
        countdown = new Countdown();
        countdown.setContext(Countdown.Context.UNKNOWN);
        scoreboardLobby.load();

        task = SGames.INSTANCE.getScheduler().repeat(this::tick, 1);
    }

    public Countdown getCountdown() {
        return countdown;
    }

    public Location getPoint() {
        return spawnPoint;
    }

    public void setPoint(Location spawnPoint) {
        this.spawnPoint = spawnPoint;
    }

    public void addToQueue(Player player) {
        queue.put(player, player.getLocation());
        player.teleport(spawnPoint);
        player.setGameMode(GameMode.ADVENTURE);
        player.getInventory().clear();
        player.setHealth(20);
        player.setFoodLevel(20);
        Collection<PotionEffect> effects = player.getActivePotionEffects();
        for (PotionEffect effect : effects) {
            player.removePotionEffect(effect.getType());
        }
        scoreboardLobby.addPlayer(player);
        SGames.INSTANCE.getScheduler().schedule(() -> SGames.INSTANCE.getMatchManager().getVoteManager().openBook(player, true), 2L);
        callEvent(new QueueRemoveEvent(this, player));
        if (isReady() && !isLoaded()) {
            load();
        }
    }

    public void removeFromQueue(Player player) {
        if (queue.containsKey(player)) player.teleport(queue.remove(player));
        scoreboardLobby.removePlayer(player);
        callEvent(new QueueRemoveEvent(this, player));
        if (!isReady() && isLoaded()) {
            callEvent(new CancelledStartEvent(this));
            unload();
        }
    }

    public boolean isReady() {
        return queue.size() >= SGames.INSTANCE.getConfigManager().getInt(ConfigKey.MIN_PLAYERS);
    }

    public void callEvent(Event e) {
        Bukkit.getPluginManager().callEvent(e);
    }

    public Collection<Player> queue() {
        return queue.keySet();
    }

    @Override
    public void load() {
        if (isLoaded()) return;
        callEvent(new ReadyToStartEvent(this));
        countdown.setContext(Countdown.Context.STARTING);
        Bukkit.getPluginManager().registerEvents(this, SGames.INSTANCE);
    }

    @Override
    public void unload() {
        if (!isLoaded()) return;
        countdown.setContext(Countdown.Context.UNKNOWN);
        HandlerList.unregisterAll(this);
    }

    @Override
    public void tick() {
        if (countdown.getContext() == Countdown.Context.STARTING) countdown.tick();
        scoreboardLobby.tick();
    }

    @Override
    public boolean isLoaded() {
        return countdown.getContext() == Countdown.Context.STARTING;
    }

    @EventHandler
    public void onCountdownEnd(CountdownEndEvent e) {
        if (e.getCountdown() == countdown && SGames.INSTANCE.getMatchManager().getMatch() == null) {
            scoreboardLobby.unload();
            task.cancel();
            task = null;
            for (Player player : queue()) {
                player.addPotionEffect(PotionEffectType.BLINDNESS.createEffect(1000, 1));
            }
            callEvent(new MatchInitEvent(this));
            try {
                SGames.INSTANCE.getMatchManager().loadMatchAsync().get();
            } catch (Exception ex) {
                try {
                    SGames.INSTANCE.getMatchManager().loadMatchAsync().get();
                } catch (Exception exc) {
                    exc.printStackTrace();
                }
            }
            unload();
        }
    }
}
