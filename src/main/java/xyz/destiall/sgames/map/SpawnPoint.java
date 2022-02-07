package xyz.destiall.sgames.map;

import org.bukkit.Location;

public class SpawnPoint {
    private final MapInfo map;
    private final Location location;

    public SpawnPoint(MapInfo map, Location point) {
        this.map = map;
        this.location = point;
    }

    public Location getLocation() {
        return location;
    }

    public MapInfo getMapInfo() {
        return map;
    }

    public SpawnPoint clone() {
        return new SpawnPoint(map, location.clone());
    }
}
