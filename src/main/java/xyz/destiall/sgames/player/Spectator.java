package xyz.destiall.sgames.player;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;
import xyz.destiall.sgames.api.SGPlayer;
import xyz.destiall.sgames.match.Match;

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
            ItemStack[] otherContents = other.getContents();
            ItemStack[] selfContents = getInventory().getContents();
            for (int i = 0; i < selfContents.length; i++) {
                if (selfContents[i] == null && otherContents[i] == null) continue;
                if (selfContents[i] != null && otherContents[i] == null) {
                    getInventory().setItem(i, null);
                    continue;
                }
                if (selfContents[i] == null && otherContents[i] != null) {
                    getInventory().setItem(i, otherContents[i]);
                    continue;
                }
                if (selfContents[i].equals(otherContents[i])) continue;

                if (selfContents[i].isSimilar(otherContents[i])) {
                    getInventory().getItem(i).setAmount(otherContents[i].getAmount());
                    continue;
                }
                getInventory().getItem(i).setType(otherContents[i].getType());
                getInventory().getItem(i).setAmount(otherContents[i].getAmount());
            }
            // player.getInventory().setContents(otherContents);
            player.getInventory().setArmorContents(other.getArmorContents());
            player.getInventory().setExtraContents(other.getExtraContents());
        }
    }

    public void copyVelocity(Vector velocity) {
        if (player != null) {
            player.setVelocity(velocity);
        }
    }
}
