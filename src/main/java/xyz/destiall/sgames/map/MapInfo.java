package xyz.destiall.sgames.map;

import org.bukkit.Location;
import org.bukkit.configuration.file.YamlConfiguration;
import xyz.destiall.sgames.SGames;
import xyz.destiall.sgames.api.Module;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MapInfo implements Module {
    private final List<SpawnPoint> spawnPoints;
    private final File mapFolder;
    private YamlConfiguration yaml;
    private String name;
    private Location center;

    public MapInfo(File mapFolder) {
        this.mapFolder = mapFolder;
        spawnPoints = new ArrayList<>();
        File mapYml = new File(mapFolder, "map.yml");
        if (!mapYml.exists()) {
            SGames.INSTANCE.getLogger().warning(mapFolder.getName() + " does not have a map.yml! Skipping...");
            return;
        }
        this.yaml = YamlConfiguration.loadConfiguration(new File(mapFolder, "map.yml"));
        this.name = yaml.getString("name", "A map");
    }

    @Override
    public void load() {
        if (yaml == null) return;
        spawnPoints.clear();
        List<?> spawnPoints = yaml.getList("spawnpoints");
        if (spawnPoints == null) {
            SGames.INSTANCE.getLogger().severe("No spawnpoints found for " + name + "!");
            return;
        }

        double maxX = 0, minX = 0, maxY = 0, minY = 0, maxZ = 0, minZ = 0;
        for (Object object : spawnPoints) {
            if (!(object instanceof java.util.Map<?,?>)) {
                SGames.INSTANCE.getLogger().severe("[" + name + "] This spawnpoint is not a map object! Skipping...");
                continue;
            }
            java.util.Map<Object, Object> spawnpoint = (Map<Object, Object>) object;
            Number x = (Number) spawnpoint.get("x");
            Number y = (Number) spawnpoint.get("y");
            Number z = (Number) spawnpoint.get("z");
            Number yaw = (Number) spawnpoint.get("yaw");
            Number pitch = (Number) spawnpoint.get("pitch");
            Location location = new Location(null, x.doubleValue(), y.doubleValue(), z.doubleValue(), yaw.floatValue(), pitch.floatValue());
            SpawnPoint spawnPoint = new SpawnPoint(this, location);
            this.spawnPoints.add(spawnPoint);
            minX = Math.min(location.getX(), minX);
            maxX = Math.max(location.getX(), maxX);
            minY = Math.min(location.getY(), minY);
            maxY = Math.max(location.getY(), maxY);
            minZ = Math.min(location.getZ(), minZ);
            maxZ = Math.max(location.getZ(), maxZ);
        }
        double xLength = maxX - minX;
        double yLength = maxY - minY;
        double zLength = maxZ - minZ;
        center = new Location(null, minX+(xLength*0.5f), minY+(yLength*0.5f), minZ+(zLength*0.5f));
    }

    @Override
    public boolean isLoaded() {
        return spawnPoints.size() != 0;
    }

    public String getName() {
        return name;
    }

    public File getMapFolder() {
        return mapFolder;
    }

    public Location getCenter() {
        return center;
    }

    @Override
    public void unload() {
        spawnPoints.clear();
    }

    public List<SpawnPoint> getSpawnPoints() {
        return spawnPoints;
    }
}
