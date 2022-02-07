package xyz.destiall.sgames.session;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Player;
import xyz.destiall.sgames.SGames;
import xyz.destiall.sgames.utils.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class CreationSession {
    public static HashMap<UUID, CreationSession> SESSIONS = new HashMap<>();

    private final UUID uuid;
    private final List<Location> spawnPoints;
    private final World world;
    private String name;

    private CreationSession(Player player) {
        this.uuid = player.getUniqueId();
        this.world = player.getWorld();
        SESSIONS.put(uuid, this);
        spawnPoints = new ArrayList<>();
        player.sendMessage(ChatColor.GREEN + "Now setting a map in world " + world.getName());
    }

    public static CreationSession of(Player player) {
        if (SESSIONS.containsKey(player.getUniqueId())) return SESSIONS.get(player.getUniqueId());
        return new CreationSession(player);
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public List<Location> getSpawnPoints() {
        return spawnPoints;
    }

    public World getWorld() {
        return world;
    }

    public boolean save() throws IOException {
        if (name == null) return false;
        if (spawnPoints.isEmpty()) return false;
        world.setAnimalSpawnLimit(0);
        world.setMonsterSpawnLimit(0);
        for (Entity e : world.getEntities()) {
            if (e instanceof Player || e instanceof ItemFrame || e instanceof ArmorStand || e instanceof Minecart) continue;
            e.remove();
        }
        File mapYml = new File(world.getWorldFolder(), "map.yml");
        if (!mapYml.exists()) mapYml.createNewFile();
        YamlConfiguration config = YamlConfiguration.loadConfiguration(mapYml);
        config.set("name", name);
        List<Map<Object, Object>> points = new ArrayList<>();
        for (Location location : spawnPoints) {
            HashMap<Object, Object> map = new HashMap<>();
            map.put("x", location.getX());
            map.put("y", location.getY());
            map.put("z", location.getZ());
            map.put("yaw", location.getYaw());
            map.put("pitch", location.getPitch());
            points.add(map);
        }
        config.set("spawnpoints", points);
        config.save(mapYml);

        File mapsFolder = SGames.INSTANCE.getMapManager().getMapsFolder();
        File mapDst = new File(mapsFolder, name + File.separator);
        if (!mapDst.exists()) mapDst.mkdir();
        else FileUtils.delete(mapDst);

        world.save();
        FileUtils.copy(world.getWorldFolder(), mapDst, true);

        return SESSIONS.remove(uuid) != null;
    }
}
