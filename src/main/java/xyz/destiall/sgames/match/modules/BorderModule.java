package xyz.destiall.sgames.match.modules;

import xyz.destiall.sgames.SGames;
import xyz.destiall.sgames.api.Module;
import xyz.destiall.sgames.config.ConfigKey;
import xyz.destiall.sgames.match.Match;

public class BorderModule implements Module {
    private final Match match;

    public BorderModule(Match match) {
        this.match = match;
    }

    @Override
    public void load() {
        match.getWorld().getWorldBorder().setCenter(match.getMap().getCenter());
        match.getWorld().getWorldBorder().setSize(SGames.INSTANCE.getConfigManager().getInt(ConfigKey.BORDER_SIZE));
    }
}
