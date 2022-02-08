package xyz.destiall.sgames.manager;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.inventory.Book;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.serializer.bungeecord.BungeeComponentSerializer;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import xyz.destiall.sgames.SGames;
import xyz.destiall.sgames.api.Module;
import xyz.destiall.sgames.lobby.events.QueueRemoveEvent;
import xyz.destiall.sgames.map.MapInfo;
import xyz.destiall.sgames.match.events.MatchInitEvent;
import xyz.destiall.sgames.utils.TextColorUtils;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;
import java.util.stream.Collectors;

public class VoteManager implements Module, Listener {
    private final HashMap<MapInfo, Set<UUID>> votingMaps;
    private final HashMap<MapInfo, Double> mapScores;
    private final SGames plugin;

    public VoteManager(SGames plugin) {
        this.plugin = plugin;
        this.votingMaps = new HashMap<>();
        this.mapScores = new HashMap<>();
    }

    public Set<Map.Entry<MapInfo, Set<UUID>>> getVotingMaps() {
        return votingMaps.entrySet();
    }

    @Override
    public void load() {
        MapManager mapManager = plugin.getMapManager();
        for (MapInfo map : mapManager.getMapInfos()) {
            this.mapScores.put(map, 2.0);
        }

        // Sorting beforehand, saves future key remaps, as bigger values are placed at the end
        List<MapInfo> sortedDist =
                mapScores.entrySet().stream()
                        .sorted(Comparator.comparingDouble(Map.Entry::getValue))
                        .map(Map.Entry::getKey)
                        .collect(Collectors.toList());

        NavigableMap<Double, MapInfo> cumulativeScores = new TreeMap<>();
        double maxWeight = cummulativeMap(0, sortedDist, cumulativeScores);

        for (int i = 0; i < 5; i++) {
            NavigableMap<Double, MapInfo> subMap =
                    cumulativeScores.tailMap(Math.random() * maxWeight, true);
            Map.Entry<Double, MapInfo> selected = subMap.pollFirstEntry();

            if (selected == null) break; // No more maps to poll
            votingMaps.put(selected.getValue(), new HashSet<>()); // Add map to votes
            if (votingMaps.size() >= 5) break; // Skip replace logic after all maps have been selected

            // Remove map from pool, updating cumulative scores
            double selectedWeight = getWeight(selected.getValue());
            maxWeight -= selectedWeight;

            NavigableMap<Double, MapInfo> temp = new TreeMap<>();
            cummulativeMap(selected.getKey() - selectedWeight, subMap.values(), temp);

            subMap.clear();
            cumulativeScores.putAll(temp);
        }

        Bukkit.getPluginManager().registerEvents(this, plugin);
        plugin.getLogger().info("Loaded VoteManager");
    }

    private double getWeight(MapInfo map) {
        return getWeight(mapScores.get(map));
    }

    public static double getWeight(Double score) {
        if (score == null || score <= 0) return 0;
        return Math.max(Math.pow(score, 2), Double.MIN_VALUE);
    }

    private double cummulativeMap(
            double currWeight, Collection<MapInfo> maps, Map<Double, MapInfo> result) {
        for (MapInfo map : maps) {
            double score = getWeight(map);
            if (score > 0) result.put(currWeight += score, map);
        }
        return currWeight;
    }

    @Override
    public void unload() {
        votingMaps.clear();
        HandlerList.unregisterAll(this);
        plugin.getLogger().info("Unloading VoteManager");
    }

    @Override
    public boolean isLoaded() {
        return votingMaps.size() != 0;
    }

    public boolean toggleVote(UUID uuid, MapInfo map) {
        Set<UUID> votes = votingMaps.get(map);
        if (votes == null) return false;
        if (votes.contains(uuid)) {
            votes.remove(uuid);
        } else {
            votes.add(uuid);
        }
        return true;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void queueRemove(QueueRemoveEvent e) {
        for (Set<UUID> votes : votingMaps.values()) {
            votes.remove(e.getPlayer().getUniqueId());
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void book(PlayerInteractEvent e) {
        if (e.getItem() == null) return;
        if (e.getItem().getType() == Material.WRITTEN_BOOK) {
            e.setCancelled(true);
            openBook(e.getPlayer(), false);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void inventory(InventoryClickEvent e) {
        e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void inventory(InventoryDragEvent e) {
        e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void drop(PlayerDropItemEvent e) {
        e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void swap(PlayerSwapHandItemsEvent e) {
        e.setCancelled(true);
    }

    @EventHandler
    public void lobbyEnd(MatchInitEvent e) {
        for (Player player : e.getLobby().queue()) {
            player.getInventory().clear();
        }
    }

    public void openBook(Player player, boolean setBook) {
        Audience audience = SGames.INSTANCE.getPluginAudience().player(player);
        Book.Builder builder = Book.builder();
        Component page = Component.empty()
                .append(Component.newline())
                .append(Component.newline());

        page = page.append(Component.text("Vote for a map", TextColorUtils.GOLD))
                .append(Component.newline())
                .append(Component.newline())
                .append(Component.newline());
        for (MapInfo map : votingMaps.keySet()) {
            page = page.append(getMapVoteComponent(player, map));
        }
        builder.addPage(page);
        audience.openBook(builder.build());

        if (setBook) {
            ItemStack book = new ItemStack(Material.WRITTEN_BOOK);
            BookMeta meta = (BookMeta) book.getItemMeta();
            if (meta == null) return;
            meta.setAuthor(SGames.INSTANCE.getName());
            meta.setTitle("" + ChatColor.LIGHT_PURPLE + ChatColor.BOLD + "Vote Book");
            meta.setDisplayName(meta.getTitle());
            meta.setGeneration(BookMeta.Generation.ORIGINAL);
            meta.spigot().setPages(BungeeComponentSerializer.get().serialize(page));
            book.setItemMeta(meta);

            player.getInventory().setItem(0, book);
            player.getInventory().setHeldItemSlot(0);
        }
    }

    public Component getMapVoteComponent(Player player, MapInfo map) {
        Component component = Component.empty();
        Set<UUID> votes = votingMaps.get(map);
        if (votes.contains(player.getUniqueId())) {
            component = component.append(Component.text("[✓]", TextColorUtils.DARK_GREEN));
        } else {
            component = component.append(Component.text("[X]", TextColorUtils.RED));
        }
        component = component
                .clickEvent(ClickEvent.runCommand("/sgames vote " + map.getName()))
                .hoverEvent(HoverEvent.showText(Component.text("Click to vote for " + map.getName(), TextColorUtils.DARK_PURPLE)));
        return component.append(Component.text(" " + map.getName(), TextColorUtils.DARK_PURPLE)).append(Component.newline());
    }

    public MapInfo getVotedMap() {
        return votingMaps.entrySet().stream()
                .max(Comparator.comparingInt(e -> e.getValue().size()))
                .map(Map.Entry::getKey)
                .orElse(null);
    }
}
