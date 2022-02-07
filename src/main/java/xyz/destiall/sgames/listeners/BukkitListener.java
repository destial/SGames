package xyz.destiall.sgames.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockCookEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockFadeEvent;
import org.bukkit.event.block.BlockFertilizeEvent;
import org.bukkit.event.block.BlockFormEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.LeavesDecayEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import xyz.destiall.sgames.SGames;
import xyz.destiall.sgames.api.Killer;
import xyz.destiall.sgames.api.SGPlayer;
import xyz.destiall.sgames.config.ConfigKey;
import xyz.destiall.sgames.config.MessageKey;
import xyz.destiall.sgames.countdown.Countdown;
import xyz.destiall.sgames.lobby.Lobby;
import xyz.destiall.sgames.match.Match;
import xyz.destiall.sgames.player.Competitor;
import xyz.destiall.sgames.player.events.CompetitorDeathEvent;

public class BukkitListener implements Listener {
    private final SGames plugin;

    public BukkitListener(SGames plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void playerLogin(PlayerLoginEvent e) {
        Match match = plugin.getMatchManager().getMatch();
        if (match != null) {
            if (plugin.getConfigManager().getBoolean(ConfigKey.ALLOW_SPECTATOR)) return;
            e.disallow(PlayerLoginEvent.Result.KICK_OTHER, plugin.getConfigManager().getMessage(MessageKey.GAME_STARTED));
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void playerJoin(PlayerJoinEvent e) {
        Match match = plugin.getMatchManager().getMatch();
        if (match != null) {
            e.setJoinMessage(null);
            match.addSpectator(e.getPlayer(), true);
            return;
        }
        Lobby lobby = plugin.getMatchManager().getLobby();
        lobby.addToQueue(e.getPlayer());
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void playerLeave(PlayerQuitEvent e) {
        Match match = plugin.getMatchManager().getMatch();
        if (match != null) {
            SGPlayer player = match.getPlayer(e.getPlayer().getUniqueId());
            if (player == null) return;
            if (player.isPlaying()) {
                Competitor competitor = (Competitor) player;
                competitor.quit();
                match.callEvent(new CompetitorDeathEvent(competitor, new Killer("Unknown", null, null)));
                match.removeCompetitor(player.getId());
            } else {
                match.removeSpectator(player.getId());
            }
            e.setQuitMessage(null);
            return;
        }
        Lobby lobby = plugin.getMatchManager().getLobby();
        lobby.removeFromQueue(e.getPlayer());
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void playerChat(AsyncPlayerChatEvent e) {
        Match match = plugin.getMatchManager().getMatch();
        if (match == null) return;

        SGPlayer player = match.getPlayer(e.getPlayer().getUniqueId());
        if (player.isSpectating()) {
            e.setFormat(SGames.INSTANCE.getConfigManager().getMessage(MessageKey.SPECTATE_PREFIX) + e.getFormat());
            match.forEachCompetitor((c) -> {
                if (c.getBukkit().isPresent()) e.getRecipients().remove(c.getBukkit().get());
            });
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void blockBreak(BlockBreakEvent e) {
        Match match = plugin.getMatchManager().getMatch();
        if (match == null) e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void blockPlace(BlockPlaceEvent e) {
        Match match = plugin.getMatchManager().getMatch();
        if (match == null) e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void blockExplode(BlockExplodeEvent e) {
        e.setCancelled(true);
        e.setYield(0);
        e.blockList().clear();
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void entityExplode(EntityExplodeEvent e) {
        e.setCancelled(true);
        e.setYield(0);
        e.blockList().clear();
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void mobSpawning(CreatureSpawnEvent e) {
        e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void decay(LeavesDecayEvent e) {
        e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void cook(BlockCookEvent e) {
        e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void fertilize(BlockFertilizeEvent e) {
        e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void fade(BlockFadeEvent e) {
        e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void form(BlockFormEvent e) {
        e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void hunger(FoodLevelChangeEvent e) {
        if (!(e.getEntity() instanceof Player)) return;
        Match match = plugin.getMatchManager().getMatch();
        if (e.getEntity().getFoodLevel() < e.getFoodLevel()) return;
        if (match == null ||
            match.getCountdown().getContext() == Countdown.Context.STARTING ||
            match.getCountdown().getContext() == Countdown.Context.GRACE ||
            match.getCountdown().getContext() == Countdown.Context.STARTING_DM)
            e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void damage(EntityDamageEvent e) {
        if (!(e.getEntity() instanceof Player)) return;
        Match match = plugin.getMatchManager().getMatch();
        if (match == null ||
            match.getCountdown().getContext() == Countdown.Context.STARTING ||
            match.getCountdown().getContext() == Countdown.Context.GRACE ||
            match.getCountdown().getContext() == Countdown.Context.STARTING_DM)
            e.setCancelled(true);
    }
}
