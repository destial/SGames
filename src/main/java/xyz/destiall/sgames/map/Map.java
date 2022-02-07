package xyz.destiall.sgames.map;

import org.bukkit.Bukkit;
import org.bukkit.World;
import xyz.destiall.sgames.SGames;
import xyz.destiall.sgames.api.Module;
import xyz.destiall.sgames.config.ConfigKey;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

public class Map implements Module {
    private final List<SpawnPoint> points;
    private final MapInfo mapInfo;
    private final World world;

    public Map(MapInfo mapInfo, World world) {
        this.mapInfo = mapInfo;
        this.world = world;
        points = mapInfo.getSpawnPoints().stream().map(SpawnPoint::clone).collect(Collectors.toList());
        if (SGames.INSTANCE.getConfigManager().getBoolean(ConfigKey.RANDOM_SPAWNPOINT)) {
            Collections.shuffle(points);
        }
        for (SpawnPoint sp : points) {
            sp.getLocation().setWorld(world);
            this.world.loadChunk(sp.getLocation().getBlockX(), sp.getLocation().getBlockZ(), true);
        }
    }

    public World getWorld() {
        return world;
    }

    public MapInfo getInfo() {
        return mapInfo;
    }

    @Override
    public void unload() {
        points.clear();
        if (world != null) {
            Bukkit.unloadWorld(world, false);
        }
    }

    public List<SpawnPoint> getPoints() {
        return points;
    }

    @Override
    public boolean isLoaded() {
        return !points.isEmpty();
    }

    public Iterator<SpawnPoint> iterator() {
        return points.iterator();
    }

    public SpawnPoint getFirstSpawnPoint() {
        return points.get(0);
    }
}
