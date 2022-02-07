package xyz.destiall.sgames.chest;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.inventory.Inventory;
import xyz.destiall.sgames.SGames;
import xyz.destiall.sgames.api.SGPlayer;
import xyz.destiall.sgames.manager.ChestManager;

import java.util.Random;

public class SGChest {
    private final Block block;
    private final Location location;
    private final Inventory inventory;
    private final Random random;
    private final ChestManager chestManager;
    private Tier tier;

    public SGChest(Block block, Inventory inventory) {
        this.block = block;
        this.location = block.getLocation();
        this.inventory = inventory;
        this.chestManager = SGames.INSTANCE.getChestManager();
        random = new Random();
        this.tier = Tier.ONE;
        for (Tier t : Tier.values()) {
            if (block.getType() == t.getType()) {
                tier = t;
                break;
            }
        }
    }

    public Location getLocation() {
        return location;
    }

    public Inventory getInventory() {
        return inventory;
    }

    public Block getBlock() {
        return block;
    }

    public void open(SGPlayer player) {
        player.openInventory(inventory);
        if (player.isPlaying()) {
            Chest chest = (Chest) block.getState();
            chest.open();
            playSound(true);
        }
    }

    public void close(SGPlayer player) {
        if (player.isPlaying()) {
            Chest chest = (Chest) block.getState();
            chest.close();
            playSound(false);
        }
    }

    public void playSound(boolean open) {
        World w;
        if ((w = location.getWorld()) == null) return;
        w.playSound(location, open ? Sound.BLOCK_CHEST_OPEN : Sound.BLOCK_CHEST_CLOSE, 1f, 1);
    }

    public void refill() {
        getInventory().clear();
        int filledSpots = 0;
        while (filledSpots < chestManager.getMinContents()) {
            for (int i = 0; i < getInventory().getSize(); i++) {
                if (random.nextInt(chestManager.getProbability()) == 0) {
                    if (getInventory().getItem(i) == null) {
                        getInventory().setItem(i, chestManager.getRandomContent(tier));
                        filledSpots++;
                    }
                }
                if (filledSpots >= chestManager.getMaxContents()) break;
            }
        }
    }

    public enum Tier {
        ONE(Material.CHEST, "Tier 1"),
        TWO(Material.TRAPPED_CHEST, "Tier 2"),
        THREE(Material.ENDER_CHEST, "Tier 3")

        ;
        private final Material type;
        private final String name;
        Tier(Material type, String name) {
            this.type = type;
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public Material getType() {
            return type;
        }

        public static Tier getTier(Material material) {
            for (Tier tier : values()) {
                if (tier.type == material) return tier;
            }
            return null;
        }
    }
}
