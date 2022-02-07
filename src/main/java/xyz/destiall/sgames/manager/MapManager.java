package xyz.destiall.sgames.manager;

import xyz.destiall.sgames.SGames;
import xyz.destiall.sgames.api.Module;
import xyz.destiall.sgames.map.MapInfo;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MapManager implements Module {
    private final SGames plugin;
    private final File mapsFolder;
    private final List<MapInfo> mapInfos;

    public MapManager(SGames plugin) {
        this.plugin = plugin;
        this.mapInfos = new ArrayList<>();
        mapsFolder = new File("maps" + File.separator);
        if (!mapsFolder.exists()) mapsFolder.mkdir();
    }

    @Override
    public void load() {
        File[] folders = mapsFolder.listFiles((f, s) -> f != null && f.isDirectory());
        if (folders == null || folders.length == 0) return;
        for (File mapFolder : folders) {
            MapInfo mapInfo = new MapInfo(mapFolder);
            mapInfo.load();
            if (mapInfo.isLoaded()) {
                mapInfos.add(mapInfo);
            } else {
                plugin.getLogger().warning("Could not load map " + mapInfo.getName());
            }
        }
        plugin.getLogger().info("Loaded " + mapInfos.size() + " maps!");
    }

    @Override
    public void unload() {
        for (MapInfo mapInfo : mapInfos) {
            mapInfo.unload();
        }
        mapInfos.clear();
    }

    @Override
    public boolean isLoaded() {
        return !mapInfos.isEmpty();
    }

    public List<MapInfo> getMapInfos() {
        return mapInfos;
    }

    public Optional<MapInfo> getMapInfo(String name) {
        return mapInfos.stream().filter(m -> m.getName().equalsIgnoreCase(name) || m.getName().toLowerCase().startsWith(name.toLowerCase())).findFirst();
    }

    public File getMapsFolder() {
        return mapsFolder;
    }
}
