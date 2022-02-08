package xyz.destiall.sgames.player.modules;

import net.kyori.adventure.text.Component;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryInteractEvent;
import org.bukkit.event.player.PlayerAnimationEvent;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.event.player.PlayerBedLeaveEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.scheduler.BukkitTask;
import xyz.destiall.sgames.SGames;
import xyz.destiall.sgames.api.Module;
import xyz.destiall.sgames.match.Match;
import xyz.destiall.sgames.player.Competitor;
import xyz.destiall.sgames.player.Spectator;
import xyz.destiall.sgames.player.events.CompetitorDeathEvent;
import xyz.destiall.sgames.utils.TextColorUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class SpectateModule implements Module, Listener {
    private final Match match;
    private final ConcurrentHashMap<UUID, Competitor> viewing;
    private final BukkitTask task;

    public SpectateModule(Match match) {
        this.match = match;
        this.viewing = new ConcurrentHashMap<>();
        task = SGames.INSTANCE.getScheduler().repeatTick(this::tick, 1);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void interact(PlayerInteractAtEntityEvent e) {
        Spectator spectator = match.getSpectator(e.getPlayer().getUniqueId());
        if (spectator == null) return;
        Competitor competitor = match.getCompetitor(e.getRightClicked().getUniqueId());
        if (competitor == null) return;
        spectate(spectator, competitor);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void sneak(PlayerToggleSneakEvent e) {
        if (!e.isSneaking()) return;
        if (viewing.containsKey(e.getPlayer().getUniqueId())) {
            unspectate(e.getPlayer().getUniqueId());
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void animation(PlayerAnimationEvent e) {
        if (viewing.containsKey(e.getPlayer().getUniqueId())) {
            e.setCancelled(true);
            return;
        }
        for (Map.Entry<UUID, Competitor> entry : viewing.entrySet()) {
            if (entry.getValue().getId().equals(e.getPlayer().getUniqueId())) {
                Spectator spectator = match.getSpectator(entry.getKey());
                if (spectator == null) continue;
                if (spectator.getBukkit().isPresent()) {
                    spectator.getBukkit().get().swingMainHand();
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void bed(PlayerBedEnterEvent e) {
        if (viewing.containsKey(e.getPlayer().getUniqueId())) e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void bed(PlayerBedLeaveEvent e) {
        if (viewing.containsKey(e.getPlayer().getUniqueId())) e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void click(InventoryClickEvent e) {
        inventory(e);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void drag(InventoryDragEvent e) {
        inventory(e);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void inventory(InventoryInteractEvent e) {
        if (viewing.containsKey(e.getWhoClicked().getUniqueId())) {
            e.setCancelled(true);
            return;
        }
        for (Map.Entry<UUID, Competitor> entry : viewing.entrySet()) {
            if (entry.getValue().getId().equals(e.getWhoClicked().getUniqueId())) {
                Spectator spectator = match.getSpectator(entry.getKey());
                if (spectator == null) continue;
                if (spectator.getBukkit().isPresent()) {
                    spectator.copyInventory(entry.getValue().getInventory());
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void pick(EntityPickupItemEvent e) {
        if (e.getEntity() instanceof Player) {
            if (viewing.containsKey(e.getEntity().getUniqueId())) {
                e.setCancelled(true);
                return;
            }
            for (Map.Entry<UUID, Competitor> entry : viewing.entrySet()) {
                if (entry.getValue().getId().equals(e.getEntity().getUniqueId())) {
                    Spectator spectator = match.getSpectator(entry.getKey());
                    if (spectator == null) continue;
                    if (spectator.getBukkit().isPresent()) {
                        spectator.copyInventory(entry.getValue().getInventory());
                    }
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void drop(PlayerDropItemEvent e) {
        if (match.getSpectator(e.getPlayer().getUniqueId()) != null) {
            e.setCancelled(true);
            return;
        }
        for (Map.Entry<UUID, Competitor> entry : viewing.entrySet()) {
            if (entry.getValue().getId().equals(e.getPlayer().getUniqueId())) {
                Spectator spectator = match.getSpectator(entry.getKey());
                if (spectator == null) continue;
                if (spectator.getBukkit().isPresent()) {
                    spectator.copyInventory(entry.getValue().getInventory());
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void projectile(ProjectileLaunchEvent e) {
        if (e.getEntity().getShooter() instanceof Player) {
            Player player = (Player) e.getEntity().getShooter();
            if (match.getSpectator(player.getUniqueId()) != null) {
                e.setCancelled(true);
                return;
            }
            for (Map.Entry<UUID, Competitor> entry : viewing.entrySet()) {
                if (entry.getValue().getId().equals(player.getUniqueId())) {
                    Spectator spectator = match.getSpectator(entry.getKey());
                    if (spectator == null) continue;
                    if (spectator.getBukkit().isPresent()) {
                        spectator.copyInventory(entry.getValue().getInventory());
                    }
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void damage(EntityDamageEvent e) {
        if (viewing.containsKey(e.getEntity().getUniqueId())) e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void damageEntity(EntityDamageByEntityEvent e) {
        if (match.getSpectator(e.getDamager().getUniqueId()) != null) e.setCancelled(true);
        damage(e);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void damageBlock(EntityDamageByBlockEvent e) {
        damage(e);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void swap(PlayerSwapHandItemsEvent e) {
        if (viewing.containsKey(e.getPlayer().getUniqueId())) e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void hotbar(PlayerItemHeldEvent e) {
        if (viewing.containsKey(e.getPlayer().getUniqueId())) {
            e.setCancelled(true);
            return;
        }
        for (Map.Entry<UUID, Competitor> entry : viewing.entrySet()) {
            if (entry.getValue().getId().equals(e.getPlayer().getUniqueId())) {
                Spectator spectator = match.getSpectator(entry.getKey());
                if (spectator == null) continue;
                if (spectator.getBukkit().isPresent()) {
                    spectator.getBukkit().get().getInventory().setHeldItemSlot(e.getNewSlot());
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void death(CompetitorDeathEvent e) {
        List<UUID> remove = new ArrayList<>();
        for (Map.Entry<UUID, Competitor> entry : viewing.entrySet()) {
            if (entry.getValue() == e.getCompetitor()) remove.add(entry.getKey());
        }
        for (UUID uuid : remove) {
            unspectate(uuid);
        }
        if (!e.isQuit())
            e.getCompetitor().sendMessage(Component.text("You can spectate other players by right clicking on them.", TextColorUtils.GRAY));
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void leave(PlayerQuitEvent e) {
        if (viewing.remove(e.getPlayer().getUniqueId()) != null) return;

        List<UUID> remove = new ArrayList<>();
        for (Map.Entry<UUID, Competitor> entry : viewing.entrySet()) {
            if (entry.getValue().getId().equals(e.getPlayer().getUniqueId())) remove.add(entry.getKey());
        }
        for (UUID uuid : remove) {
            unspectate(uuid);
        }
    }

    @EventHandler
    public void eat(PlayerItemConsumeEvent e) {
        if (match.getSpectator(e.getPlayer().getUniqueId()) != null) {
            e.setCancelled(true);
            return;
        }
        for (Map.Entry<UUID, Competitor> entry : viewing.entrySet()) {
            if (entry.getValue().getId().equals(e.getPlayer().getUniqueId())) {
                Spectator spectator = match.getSpectator(entry.getKey());
                if (spectator == null) continue;
                if (spectator.getBukkit().isPresent()) {
                    spectator.copyInventory(entry.getValue().getInventory());
                }
            }
        }
    }

    public void spectate(Spectator spectator, Competitor competitor) {
        if (viewing.containsKey(spectator.getId())) {
            unspectate(spectator.getId());
        }
        viewing.put(spectator.getId(), competitor);
        spectator.setGameMode(GameMode.ADVENTURE);
        spectator.copyInventory(competitor.getInventory());
        spectator.copyEffects(competitor);
        spectator.hide(competitor);
        match.forEachSpectator((s) -> {
            if (s == spectator) return;
            s.hide(spectator);
        });
        spectator.showTitle(Component.text("Spectating", TextColorUtils.GRAY), Component.text(competitor.getName(), TextColorUtils.GRAY));
    }

    public void unspectate(UUID uuid) {
        Competitor competitor = viewing.remove(uuid);
        if (competitor != null) {
            Spectator spectator = match.getSpectator(uuid);
            if (spectator == null) return;
            spectator.clearInventory();
            spectator.clearEffects();
            spectator.setGameMode(GameMode.SPECTATOR);
            spectator.unhide(competitor);
            spectator.showTitle(Component.text("Exiting", TextColorUtils.GRAY), Component.text(competitor.getName(), TextColorUtils.GRAY));
        }
    }

    public boolean isSpectating(UUID uuid) {
        return viewing.containsKey(uuid);
    }

    @Override
    public void unload() {
        task.cancel();
    }

    public void tick() {
        for (Map.Entry<UUID, Competitor> entry : viewing.entrySet()) {
            Spectator spectator = match.getSpectator(entry.getKey());
            if (spectator == null) continue;
            // spectator.copyInventory(entry.getValue().getInventory());
            Location to = entry.getValue().getLocation().clone();
            spectator.teleport(to);
            spectator.copyVelocity(entry.getValue().getVelocity());
            spectator.setHealth(entry.getValue().getHealth());
            spectator.setHunger(entry.getValue().getHunger());
        }
    }
}
