package xyz.destiall.sgames.match;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.scheduler.BukkitTask;
import xyz.destiall.sgames.SGames;
import xyz.destiall.sgames.api.Module;
import xyz.destiall.sgames.api.SGPlayer;
import xyz.destiall.sgames.api.Tickable;
import xyz.destiall.sgames.chest.ChestModule;
import xyz.destiall.sgames.config.ConfigKey;
import xyz.destiall.sgames.config.MessageKey;
import xyz.destiall.sgames.countdown.Countdown;
import xyz.destiall.sgames.countdown.modules.CountdownModule;
import xyz.destiall.sgames.map.Map;
import xyz.destiall.sgames.map.SpawnPoint;
import xyz.destiall.sgames.match.events.MatchFinishEvent;
import xyz.destiall.sgames.match.events.MatchLoadEvent;
import xyz.destiall.sgames.match.events.MatchMoveEvent;
import xyz.destiall.sgames.match.modules.BorderModule;
import xyz.destiall.sgames.player.Competitor;
import xyz.destiall.sgames.player.Spectator;
import xyz.destiall.sgames.player.modules.BlockModule;
import xyz.destiall.sgames.player.modules.BossBarModule;
import xyz.destiall.sgames.player.modules.DeathModule;
import xyz.destiall.sgames.player.modules.ScoreboardModule;
import xyz.destiall.sgames.player.modules.SpectateModule;
import xyz.destiall.sgames.utils.FormatUtils;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class Match implements Module {
    private final ConcurrentHashMap<UUID, Competitor> competitors;
    private final ConcurrentHashMap<UUID, Spectator> spectators;
    private final List<Module> modules;
    private final List<Tickable> tickables;
    private final Map map;
    private BukkitTask tickTask;
    private Countdown countdown;

    private State state;

    public Match(Map map) {
        this.map = map;
        this.competitors = new ConcurrentHashMap<>();
        this.spectators = new ConcurrentHashMap<>();
        this.tickables = new ArrayList<>();
        this.modules = new ArrayList<>();
        state = State.UNKNOWN;
    }

    public State getState() {
        return state;
    }

    public Map getMap() {
        return map;
    }

    @Override
    public void load() {
        state = State.LOADING;

        // Make sure countdown is always the first to tick
        countdown = new Countdown();
        tickables.add(countdown);

        modules.add(new ScoreboardModule(this));
        modules.add(new SpectateModule(this));
        modules.add(new CountdownModule(this));
        modules.add(new ChestModule(this));
        modules.add(new DeathModule(this));
        modules.add(new BlockModule(this));
        modules.add(new BossBarModule(this));

        if (SGames.INSTANCE.getConfigManager().getBoolean(ConfigKey.BORDER_ENABLED)) {
            modules.add(new BorderModule(this));
        }

        for (Module module : modules) {
            module.load();
            if (module instanceof Listener) {
                Bukkit.getPluginManager().registerEvents((Listener) module, SGames.INSTANCE);
            }
            if (module instanceof Tickable) {
                tickables.add((Tickable) module);
            }
            SGames.INSTANCE.getLogger().info("Loading module " + module.getClass().getSimpleName());
        }

        callEvent(new MatchLoadEvent(this));
    }

    @Override
    public void unload() {
        state = State.UNLOADING;
        tickTask.cancel();
        map.unload();
        for (Module module : modules) {
            if (module instanceof Listener) {
                HandlerList.unregisterAll((Listener) module);
            }
            module.unload();
        }
        modules.clear();

        for (Tickable tickable : tickables) {
            if (tickable instanceof Module) {
                ((Module) tickable).unload();
            }
        }
        tickables.clear();
    }

    @Override
    public boolean isLoaded() {
        return state == State.LOADING;
    }

    public void removeCompetitor(UUID uuid) {
        competitors.remove(uuid);
    }

    public void removeSpectator(UUID uuid) {
        spectators.remove(uuid);
    }

    public void addCompetitors(Collection<Player> players) {
        SGames.INSTANCE.getLogger().info("Moving players to match");
        Iterator<SpawnPoint> sp = map.iterator();
        for (Player player : players) {
            if (!sp.hasNext()) {
                player.kickPlayer(SGames.INSTANCE.getConfigManager().getMessage(MessageKey.GAME_FULL));
                continue;
            }
            Competitor competitor = new Competitor(player, this, sp.next());
            competitors.put(player.getUniqueId(), competitor);
            competitor.clearInventory();
            competitor.clearEffects();
            competitor.setGameMode(GameMode.SURVIVAL);
        }
        callEvent(new MatchMoveEvent(this));
    }

    public void addSpectator(Player player, boolean teleport) {
        ScoreboardModule sm = getTickable(ScoreboardModule.class);
        competitors.remove(player.getUniqueId());
        Spectator spectator = new Spectator(player, this);
        spectators.put(player.getUniqueId(), spectator);
        forEachPlayer((p) -> {
            if (spectator == p) return;
            p.hide(spectator);
        });
        spectator.clearInventory();
        spectator.clearEffects();
        spectator.setGameMode(GameMode.SPECTATOR);
        sm.addPlayer(player);
        if (teleport) {
            player.teleport(map.getCenter());
        }
    }

    public boolean calculateDMConditions() {
        boolean dm = isRunning() && (competitors.size() <= SGames.INSTANCE.getConfigManager().getInt(ConfigKey.MIN_DEATHMATCH) || countdown.getContext() == Countdown.Context.RUNNING && countdown.getRemaining().isZero());
        if (dm) {
            countdown.setDuration(Duration.of(30, ChronoUnit.SECONDS));

            if (SGames.INSTANCE.getConfigManager().getBoolean(ConfigKey.DEATHMATCH_NIGHT)) {
                getWorld().setTime(12000);
            }
        }
        return dm;
    }

    public boolean calculateWinConditions() {
        boolean win = isRunning() && (competitors.size() <= 1 || countdown.getContext() == Countdown.Context.DEATHMATCH && countdown.getRemaining().isZero());
        if (win) {
            countdown.setContext(Countdown.Context.FINISHING);
            state = State.FINISHING;
            callEvent(new MatchFinishEvent(this));

            String winner = SGames.INSTANCE.getConfigManager().getMessage(MessageKey.WINNER);
            winner = FormatUtils.formatMatch(winner, this);
            broadcast(winner);

            if (SGames.INSTANCE.getConfigManager().getBoolean(ConfigKey.WIN_FIREWORKS)) {
                FireworkEffect effect = FireworkEffect.builder().trail(true).withColor(Color.YELLOW).with(FireworkEffect.Type.STAR).build();
                for (SpawnPoint sp : map.getPoints()) {
                    Firework f = getWorld().spawn(sp.getLocation(), Firework.class);
                    FireworkMeta meta = f.getFireworkMeta();
                    meta.addEffect(effect);
                }
                if (competitors.size() == 1) {
                    Competitor competitor = getWinner();
                    for (double theta = -Math.PI; theta <= Math.PI; theta += Math.PI / 6) {
                        double x = Math.cos(theta);
                        double z = Math.sin(theta);
                        Location location = new Location(competitor.getWorld(), x, competitor.getLocation().getBlockY(), z);
                        Firework f = map.getWorld().spawn(location, Firework.class);
                        FireworkMeta meta = f.getFireworkMeta();
                        meta.addEffect(effect);
                    }
                }
            }

            if (SGames.INSTANCE.getConfigManager().getBoolean(ConfigKey.WIN_NIGHT)) {
                getWorld().setTime(12000);
            }
        }
        return win;
    }

    public World getWorld() {
        return map.getWorld();
    }

    public void start() {
        state = State.RUNNING;
        teleport();
        tickTask = SGames.INSTANCE.getScheduler().repeat(() -> {
            for (Tickable tickable : tickables) {
                if (tickable instanceof Module && !((Module) tickable).isLoaded()) {
                    ((Module) tickable).load();
                }
                tickable.tick();
            }
        }, 1);
    }

    public void teleport() {
        forEachCompetitor(Competitor::teleportToSpawnPoint);
        Location spec = map.getCenter();
        forEachSpectator(spectator -> spectator.teleport(spec));
    }

    public Competitor getCompetitor(UUID uuid) {
        return competitors.get(uuid);
    }

    public Spectator getSpectator(UUID uuid) {
        return spectators.get(uuid);
    }

    public int getAlive() {
        return competitors.size();
    }

    public int getSpectating() {
        return spectators.size();
    }

    public SGPlayer getPlayer(UUID uuid) {
        SGPlayer player = getCompetitor(uuid);
        return player != null ? player : getSpectator(uuid);
    }

    public Competitor getWinner() {
        return competitors.size() != 1 ? null : competitors.values().stream().findFirst().get();
    }

    public void forEachPlayer(Consumer<SGPlayer> consumer) {
        for (Competitor competitor : competitors.values()) {
            consumer.accept(competitor);
        }
        for (Spectator spectator : spectators.values()) {
            consumer.accept(spectator);
        }
    }

    public void forEachCompetitor(Consumer<Competitor> consumer) {
        for (Competitor competitor : competitors.values()) {
            consumer.accept(competitor);
        }
    }

    public void forEachSpectator(Consumer<Spectator> consumer) {
        for (Spectator spectator : spectators.values()) {
            consumer.accept(spectator);
        }
    }

    public <T> T getTickable(Class<T> clazz) {
        Tickable tickable = tickables.stream().filter(t -> clazz.isAssignableFrom(t.getClass())).findFirst().orElse(null);
        if (tickable == null) return null;
        return clazz.cast(tickable);
    }

    public Countdown getCountdown() {
        return countdown;
    }

    public <N extends Module> N getModule(Class<N> clazz) {
        Module module = modules.stream().filter(m -> clazz.isAssignableFrom(m.getClass())).findFirst().orElse(null);
        if (module == null) return null;
        return clazz.cast(module);
    }

    public void broadcast(String message) {
        Component component = Component.text(message);
        broadcast(component);
    }

    public void broadcast(Component component) {
        Bukkit.getConsoleSender().sendMessage(LegacyComponentSerializer.legacySection().serialize(component));
        forEachPlayer((p) -> p.sendMessage(component));
    }

    public void sendTitle(String title) {
        sendTitle(title, null);
    }

    public void sendTitle(Component title) {
        sendTitle(title, null);
    }

    public void sendTitle(String title, String subtitle) {
        sendTitle(Component.text(title), subtitle != null ? Component.text(title) : Component.empty());
    }

    public void sendTitle(Component title, Component subtitle) {
        Title.Times times = Title.Times.of(Duration.of(0, ChronoUnit.SECONDS), Duration.of(1500, ChronoUnit.MILLIS), Duration.of(1, ChronoUnit.SECONDS));
        Title component = Title.title(title, subtitle == null ? Component.empty() : subtitle, times);
        forEachPlayer((p) -> p.showTitle(component));
    }

    public boolean isRunning() {
        return getState() == State.RUNNING;
    }

    public boolean isFinishing() {
        return getState() == State.FINISHING;
    }

    public void callEvent(Event event) {
        SGames.INSTANCE.getServer().getPluginManager().callEvent(event);
    }

    public enum State {
        UNKNOWN,
        LOADING,
        RUNNING,
        FINISHING,
        UNLOADING,
    }
}
