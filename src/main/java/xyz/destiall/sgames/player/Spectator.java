package xyz.destiall.sgames.player;

import org.bukkit.entity.Player;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;
import xyz.destiall.sgames.api.SGPlayer;
import xyz.destiall.sgames.match.Match;

import java.util.Arrays;
import java.util.Collection;

public class Spectator extends SGPlayer {
    public Spectator(Player player, Match match) {
        super(player, match);
    }

    public void setInvisible() {
        if (player != null) {
            player.addPotionEffect(PotionEffectType.INVISIBILITY.createEffect(10000, 1));
        }
    }

    public void setVisible() {
        if (player != null) {
            player.removePotionEffect(PotionEffectType.INVISIBILITY);
        }
    }

    public void copyEffects(SGPlayer player) {
        if (this.player != null && player.getBukkit().isPresent()) {
            Collection<PotionEffect> effects = player.getBukkit().get().getActivePotionEffects();
            for (PotionEffect effect : effects) {
                this.player.addPotionEffect(effect);
            }
        }
    }

    public void copyInventory(PlayerInventory other) {
        if (player != null) {
            if (!Arrays.equals(player.getInventory().getContents(), other.getContents())) {
                player.getInventory().setContents(other.getContents());
            }
            if (!Arrays.equals(player.getInventory().getArmorContents(), other.getArmorContents())) {
                player.getInventory().setArmorContents(other.getArmorContents());
            }
            if (!Arrays.equals(player.getInventory().getExtraContents(), other.getExtraContents())) {
                player.getInventory().setExtraContents(other.getExtraContents());
            }
        }
    }

    public void copyVelocity(Vector velocity) {
        if (player != null) {
            player.setVelocity(velocity);
        }
    }
}
