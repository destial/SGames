package xyz.destiall.sgames.player.modules;

import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.projectiles.ProjectileSource;
import xyz.destiall.sgames.SGames;
import xyz.destiall.sgames.api.Killer;
import xyz.destiall.sgames.api.Module;
import xyz.destiall.sgames.api.SGPlayer;
import xyz.destiall.sgames.config.ConfigKey;
import xyz.destiall.sgames.config.MessageKey;
import xyz.destiall.sgames.match.Match;
import xyz.destiall.sgames.player.Competitor;
import xyz.destiall.sgames.player.events.CompetitorDeathEvent;
import xyz.destiall.sgames.utils.FormatUtils;
import xyz.destiall.sgames.utils.SoundUtils;

public class DeathModule implements Module, Listener {
    private final Match match;
    public DeathModule(Match match) {
        this.match = match;
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void damageEntity(EntityDamageByEntityEvent e) {
        damage(e);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void damageBlock(EntityDamageByBlockEvent e) {
        damage(e);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void damage(EntityDamageEvent e) {
        if (!(e.getEntity() instanceof Player)) return;
        Player p = (Player) e.getEntity();
        if (e.getFinalDamage() < p.getHealth()) return;

        SGPlayer player = match.getPlayer(e.getEntity().getUniqueId());
        if (player == null) return;

        if (player.isSpectating()) {
            SpectateModule sm = match.getModule(SpectateModule.class);
            if (sm.isSpectating(player.getId())) return;
            e.setCancelled(true);
            player.setHealth(20);
            if (e.getCause() == EntityDamageEvent.DamageCause.VOID) {
                player.teleport(match.getMap().getCenter());
            }
            return;
        }

        EntityDamageEvent lastDamage = p.getLastDamageCause();
        String killerName = p.getName();
        Entity entity = null;
        Block block = null;
        if (lastDamage instanceof EntityDamageByEntityEvent) {
            EntityDamageByEntityEvent lastDamageEntity = (EntityDamageByEntityEvent) lastDamage;
            Entity damager = lastDamageEntity.getDamager();
            if (damager instanceof Projectile) {
                ProjectileSource source = ((Projectile) damager).getShooter();
                if (source instanceof Entity) {
                    entity = (Entity) source;
                    killerName = ((Entity) source).getName();
                }
            } else if (damager instanceof TNTPrimed) {
                Entity source = ((TNTPrimed) damager).getSource();
                if (source != null) {
                    entity = source;
                    killerName = source.getName();
                }
            }
        } else if (lastDamage instanceof EntityDamageByBlockEvent) {
            Block damager = ((EntityDamageByBlockEvent) lastDamage).getDamager();
            if (damager != null) {
                block = damager;
                killerName = damager.getType().name();
            }
        }

        Killer killer = new Killer(killerName, entity, block);
        match.callEvent(new CompetitorDeathEvent((Competitor) player, killer));
    }

    @EventHandler
    public void competitorDeath(CompetitorDeathEvent e) {
        Competitor p = e.getCompetitor();
        for (ItemStack item : p.getInventory()) {
            if (item == null) continue;
            p.getWorld().dropItemNaturally(p.getLocation(), item);
        }

        String message = SGames.INSTANCE.getConfigManager().getMessage(MessageKey.KILL);
        message = message.replace("{victim}", p.getName())
                .replace("{killer}", e.getKiller().getName());

        match.broadcast(message);

        if (SGames.INSTANCE.getConfigManager().getBoolean(ConfigKey.DEATH_LIGHTNING)) {
            match.getWorld().strikeLightningEffect(p.getLocation());
            match.getWorld().playSound(p.getLocation(), Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 2, 1);
        } else {
            p.playSound(SoundUtils.of(Sound.ENTITY_WITHER_HURT, 1, 1));
            if (e.getKiller().isEntity()) {
                Entity killer = e.getKiller().getEntity();
                if (killer instanceof Player) {
                    ((Player) killer).playSound(killer.getLocation(), Sound.BLOCK_NOTE_BLOCK_XYLOPHONE, 1, 1);
                }
            }
        }

        match.removeCompetitor(p.getId());
        p.reset();

        if (p.getBukkit().isPresent()) {
            String dead = FormatUtils.formatMatchPlayer(SGames.INSTANCE.getConfigManager().getMessage(MessageKey.DIED), p.getBukkit().get(), match);
            if (SGames.INSTANCE.getConfigManager().getBoolean(ConfigKey.KICK_ON_DEATH)) {
                p.kick(dead);
                p.setBukkit(null);
            } else {
                p.sendMessage(dead);
                match.addSpectator(p.getBukkit().get(), false);
            }
        }

        if (match.calculateWinConditions()) return;
        match.calculateDMConditions();
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void die(PlayerDeathEvent e) {
        e.setDeathMessage(null);
    }
}
