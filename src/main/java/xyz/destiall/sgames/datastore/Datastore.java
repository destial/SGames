package xyz.destiall.sgames.datastore;

import xyz.destiall.sgames.SGames;
import xyz.destiall.sgames.api.Module;
import xyz.destiall.sgames.api.Stats;

import java.util.HashMap;
import java.util.UUID;

public class Datastore implements Module {
    private final SGames plugin;
    private final HashMap<UUID, Stats> stats;

    public Datastore(SGames plugin) {
        stats = new HashMap<>();
        this.plugin = plugin;
    }

    public Stats getStats(UUID uuid) {
        if (stats.containsKey(uuid)) {
            return stats.get(uuid);
        }
        SQLStats s = new SQLStats(uuid);
        s.load();
        stats.put(uuid, s);
        return s;
    }

    @Override
    public void load() {

    }

    @Override
    public void unload() {

    }

    @Override
    public boolean isLoaded() {
        return true;
    }
}
