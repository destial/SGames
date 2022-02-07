package xyz.destiall.sgames.datastore;

import xyz.destiall.sgames.api.Module;
import xyz.destiall.sgames.api.Stats;

import java.util.UUID;

public class SQLStats extends Stats implements Module {
    public SQLStats(UUID uuid) {
        super(uuid);
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
