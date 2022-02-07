package xyz.destiall.sgames.manager;

import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import xyz.destiall.sgames.SGames;
import xyz.destiall.sgames.api.Module;
import xyz.destiall.sgames.chest.SGChest;
import xyz.destiall.sgames.config.ConfigKey;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ChestManager implements Module {
    private final List<ItemStack> tier1;
    private final List<ItemStack> tier2;
    private final List<ItemStack> tier3;
    private final SGames plugin;
    private final Random random;
    private final File chestYml;
    private int minContents;
    private int maxContents;
    private int probability;

    public ChestManager(SGames plugin) {
        this.plugin = plugin;
        random = new Random();
        chestYml = new File(plugin.getDataFolder(), "chest.yml");
        if (!chestYml.exists()) {
            plugin.saveResource("chest.yml", false);
        }
        tier1 = new ArrayList<>();
        tier2 = new ArrayList<>();
        tier3 = new ArrayList<>();
    }

    @Override
    public void load() {
        YamlConfiguration config = YamlConfiguration.loadConfiguration(chestYml);
        maxContents = plugin.getConfigManager().getInt(ConfigKey.CHEST_MAX_CONTENTS);
        minContents = plugin.getConfigManager().getInt(ConfigKey.CHEST_MIN_CONTENTS);
        probability = plugin.getConfigManager().getInt(ConfigKey.CHEST_PROBABILITY);

        List<String> tier1RawContent = config.getStringList("tier-1");
        for (String content : tier1RawContent) {
            ItemStack item = parse(content);
            if (item == null) continue;
            tier1.add(item);
        }

        List<String> tier2RawContent = config.getStringList("tier-2");
        for (String content : tier2RawContent) {
            ItemStack item = parse(content);
            if (item == null) continue;
            tier2.add(item);
        }

        List<String> tier3RawContent = config.getStringList("tier-3");
        for (String content : tier3RawContent) {
            ItemStack item = parse(content);
            if (item == null) continue;
            tier3.add(item);
        }
    }

    private ItemStack parse(String line) {
        String[] arguments = line.split(" : ");
        String itemName = arguments[0];
        ItemStack item = null;
        if (!itemName.isEmpty()) {
            Material material = Material.getMaterial(itemName);
            if (material != null) {
                try {
                    if (arguments.length > 1 && arguments[1] != null) {
                        item = new ItemStack(material, Integer.parseInt(arguments[1]));
                    } else {
                        item = new ItemStack(material, 1);
                    }
                } catch(Exception e) {
                    item = new ItemStack(material, 1);
                }
            }
        }
        return item;
    }

    @Override
    public void unload() {
        tier1.clear();
        tier2.clear();
        tier3.clear();
    }

    @Override
    public boolean isLoaded() {
        return !tier1.isEmpty();
    }

    public int getMinContents() {
        return minContents;
    }

    public int getMaxContents() {
        return maxContents;
    }

    public int getProbability() {
        return probability;
    }

    public ItemStack getRandomContent(SGChest.Tier tier) {
        List<ItemStack> contents = tier1;
        if (tier == SGChest.Tier.TWO) {
            contents = tier2;
        } else if (tier == SGChest.Tier.THREE) {
            contents = tier3;
        }
        return contents.get(random.nextInt(contents.size() - 1));
    }

    public boolean canBePlaced(Material material) {
        return tier1.stream().anyMatch(i -> i.getType() == material) || tier2.stream().anyMatch(i -> i.getType() == material) || tier3.stream().anyMatch(i -> i.getType() == material);
    }
}
