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
        }
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

    @Override
    public void unload() {
        spawnPoints.clear();
    }

    public List<SpawnPoint> getSpawnPoints() {
        return spawnPoints;
    }
}
