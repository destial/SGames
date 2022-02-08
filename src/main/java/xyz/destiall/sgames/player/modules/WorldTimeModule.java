package xyz.destiall.sgames.player.modules;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import xyz.destiall.sgames.SGames;
import xyz.destiall.sgames.api.Module;
import xyz.destiall.sgames.config.ConfigKey;
import xyz.destiall.sgames.match.Match;
import xyz.destiall.sgames.match.events.DeathmatchEvent;
import xyz.destiall.sgames.match.events.MatchFinishEvent;

public class WorldTimeModule implements Module, Listener {
    private final Match match;
    private final int NIGHT = 12000;

    public WorldTimeModule(Match match) {
        this.match = match;
    }

    @EventHandler
    public void dmStart(DeathmatchEvent e) {
        if (SGames.INSTANCE.getConfigManager().getBoolean(ConfigKey.DEATHMATCH_NIGHT)) {
            match.getWorld().setTime(NIGHT);
        }
    }

    @EventHandler
    public void matchFinish(MatchFinishEvent e) {
        if (SGames.INSTANCE.getConfigManager().getBoolean(ConfigKey.WIN_NIGHT)) {
            match.getWorld().setTime(NIGHT);
        }
    }
}
