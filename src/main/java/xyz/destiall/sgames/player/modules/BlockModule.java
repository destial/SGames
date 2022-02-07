package xyz.destiall.sgames.player.modules;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import xyz.destiall.sgames.SGames;
import xyz.destiall.sgames.api.Module;
import xyz.destiall.sgames.countdown.Countdown;
import xyz.destiall.sgames.manager.ChestManager;
import xyz.destiall.sgames.match.Match;
import xyz.destiall.sgames.player.Competitor;

import java.util.HashSet;
import java.util.Set;

public class BlockModule implements Module, Listener {
    private final Match match;
    private final Set<Block> breakableBlocks;

    public BlockModule(Match match) {
        this.match = match;
        breakableBlocks = new HashSet<>();
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void blockPlace(BlockPlaceEvent e) {
        if (match.getCountdown().getContext() == Countdown.Context.STARTING ||
            match.getCountdown().getContext() == Countdown.Context.STARTING_DM ||
            match.getCountdown().getContext() == Countdown.Context.GRACE ||
            match.isFinishing()) {
            e.setCancelled(true);
            return;
        }
        Block block = e.getBlockPlaced();
        if (block.getType().name().contains("LEAVES")) return;
        if (block.getType() == Material.TNT) {
            block.setType(Material.AIR);
            TNTPrimed tnt = block.getWorld().spawn(e.getBlockPlaced().getLocation().add(0.5, 0, 0.5), TNTPrimed.class);
            tnt.setSource(e.getPlayer());
            return;
        }

        ChestManager cm = SGames.INSTANCE.getChestManager();
        if (!cm.canBePlaced(block.getType())) {
            e.setCancelled(true);
            return;
        }
        breakableBlocks.add(block);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void blockBreak(BlockBreakEvent e) {
        e.setDropItems(false);
        e.setExpToDrop(0);
        Block block = e.getBlock();
        if (block.getType().name().contains("LEAVES")) return;
        if (breakableBlocks.remove(block)) return;
        e.setCancelled(true);
    }


    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void move(PlayerMoveEvent e) {
        if (e.getTo() == null) return;
        Competitor competitor = match.getCompetitor(e.getPlayer().getUniqueId());
        if (competitor == null) return;
        if (samePosition(e.getFrom(), e.getTo())) return;
        if (match.getCountdown().getContext() == Countdown.Context.STARTING ||
                match.getCountdown().getContext() == Countdown.Context.STARTING_DM) {
            Location from = e.getFrom();
            from.setY(competitor.getSpawnPoint().getLocation().getY());
            from.setYaw(e.getTo().getYaw());
            from.setPitch(e.getTo().getPitch());
            e.setTo(from);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void entityExplode(EntityExplodeEvent e) {
        World w;
        if ((w = e.getLocation().getWorld()) == null) return;
        if (match.getWorld() != w) return;
        w.createExplosion(e.getLocation(), 0, false, false, e.getEntity());
    }

    public Set<Block> getBreakableBlocks() {
        return breakableBlocks;
    }

    private boolean samePosition(Location from, Location to) {
        if (to == null) return false;
        return from.getBlockX() == to.getBlockX() && from.getBlockZ() == to.getBlockZ();
    }

    @Override
    public void unload() {
        breakableBlocks.clear();
    }
}
