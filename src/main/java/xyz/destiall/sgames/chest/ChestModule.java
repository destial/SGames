package xyz.destiall.sgames.chest;

import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Container;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryInteractEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.DoubleChestInventory;
import org.bukkit.inventory.InventoryHolder;
import xyz.destiall.sgames.SGames;
import xyz.destiall.sgames.api.Module;
import xyz.destiall.sgames.api.SGPlayer;
import xyz.destiall.sgames.config.ConfigKey;
import xyz.destiall.sgames.match.Match;
import xyz.destiall.sgames.match.events.DeathmatchEvent;

import java.util.concurrent.ConcurrentHashMap;

public class ChestModule implements Module, Listener {
    private final Match match;
    private ConcurrentHashMap<Block, SGChest> chests;

    public ChestModule(final Match match) {
        this.match = match;
    }

    @Override
    public void load() {
        this.chests = new ConcurrentHashMap<>();
    }

    @Override
    public void unload() {
        chests.clear();
    }

    @Override
    public boolean isLoaded() {
        return chests != null;
    }

    public boolean isChest(final Block block) {
        return SGChest.Tier.getTier(block.getType()) != null;
    }

    @EventHandler
    public void deathmatch(DeathmatchEvent e) {
        refillAll();
    }

    @EventHandler
    public void openChest(final PlayerInteractEvent e) {
        if (e.getAction() == Action.RIGHT_CLICK_BLOCK && e.getClickedBlock() != null && isChest(e.getClickedBlock())) {
            Block block = e.getClickedBlock();
            SGPlayer player = match.getPlayer(e.getPlayer().getUniqueId());
            if (player == null) return;
            if (chests.containsKey(block)) {
                e.setCancelled(true);
                chests.get(block).open(player);
                return;
            }
            SGChest.Tier tier = SGChest.Tier.getTier(block.getType());
            if (tier == null) return;

            BlockState state = block.getState();
            e.setCancelled(true);

            int size = 3 * 9;
            if (state instanceof Container) {
                size = ((Container) state).getInventory().getSize();
            }

            SGChest chest = new SGChest(block, Bukkit.createInventory(null, size, tier.getName()));
            if (state instanceof InventoryHolder) {
                if (((InventoryHolder) state).getInventory() instanceof DoubleChestInventory) {
                    DoubleChestInventory doubleChest = (DoubleChestInventory) (((InventoryHolder) state).getInventory());
                    Block left = doubleChest.getLeftSide().getLocation().getBlock();
                    Block right = doubleChest.getRightSide().getLocation().getBlock();
                    chests.put(left, chest);
                    chests.put(right, chest);
                }
            }
            chests.put(block, chest);

            chest.refill();
            chest.open(player);
        }
    }

    public void refillAll() {
        for (SGChest chest : chests.values()) {
            chest.refill();
        }
    }

    @EventHandler
    public void closeChest(final InventoryCloseEvent e) {
        if (SGames.INSTANCE.getConfigManager().getBoolean(ConfigKey.CHEST_KEEP_OPEN)) return;

        SGPlayer player = match.getPlayer(e.getPlayer().getUniqueId());
        if (player == null) return;
        for (SGChest chest : chests.values()) {
            if (e.getInventory() == chest.getInventory()) {
                chest.close(player);
                break;
            }
        }
    }

    @EventHandler
    public void inventoryInteract(final InventoryInteractEvent e) {
        if (e.getWhoClicked() instanceof Player) {
            Player p = (Player) e.getWhoClicked();
            SGPlayer player = match.getPlayer(p.getUniqueId());
            if (player == null) return;
            if (player.isSpectating()) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void inventoryClick(final InventoryClickEvent e) {
        inventoryInteract(e);
    }

    @EventHandler
    public void inventoryDrag(final InventoryDragEvent e) {
        inventoryInteract(e);
    }
}
