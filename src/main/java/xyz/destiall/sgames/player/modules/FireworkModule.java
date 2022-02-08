package xyz.destiall.sgames.player.modules;

import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Firework;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.meta.FireworkMeta;
import xyz.destiall.sgames.api.Module;
import xyz.destiall.sgames.api.Tickable;
import xyz.destiall.sgames.map.SpawnPoint;
import xyz.destiall.sgames.match.Match;
import xyz.destiall.sgames.match.events.MatchFinishEvent;
import xyz.destiall.sgames.player.Competitor;

public class FireworkModule implements Module, Listener, Tickable {
    private final Match match;
    private boolean fireworks;
    private Competitor winner;
    private int counter;

    public FireworkModule(Match match) {
        this.match = match;
        this.fireworks = false;
        counter = 0;
    }

    @EventHandler
    public void matchFinish(MatchFinishEvent e) {
        fireworks = true;
        winner = e.getWinner();
    }

    @Override
    public void tick() {
        if (!fireworks || counter >= 3) return;
        int i = (int) (Math.random() * 3);
        FireworkEffect effect = FireworkEffect.builder().trail(true).withColor(i == 3 ? Color.RED : i == 2 ? Color.YELLOW : i == 1 ? Color.GREEN : Color.BLUE).with(FireworkEffect.Type.STAR).flicker(true).build();
        for (SpawnPoint sp : match.getMap().getPoints()) {
            Firework f = match.getWorld().spawn(sp.getLocation(), Firework.class);
            FireworkMeta meta = f.getFireworkMeta();
            meta.addEffect(effect);
            f.setFireworkMeta(meta);
            match.getWorld().playSound(sp.getLocation(), Sound.ENTITY_FIREWORK_ROCKET_LAUNCH, 0.5f, 1);
        }
        if (winner == null) return;
        for (double theta = -Math.PI; theta <= Math.PI; theta += Math.PI / 3) {
            double x = Math.cos(theta);
            double z = Math.sin(theta);
            Location location = new Location(match.getWorld(), winner.getLocation().getBlockX() + x, winner.getLocation().getBlockY(), winner.getLocation().getBlockZ() + z);
            Firework f = match.getWorld().spawn(location, Firework.class);
            FireworkMeta meta = f.getFireworkMeta();
            meta.addEffect(effect);
            f.setFireworkMeta(meta);
        }
        counter++;
    }
}
