package xyz.destiall.sgames.player.modules;

import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import xyz.destiall.sgames.SGames;
import xyz.destiall.sgames.api.Module;
import xyz.destiall.sgames.api.Tickable;
import xyz.destiall.sgames.config.MessageKey;
import xyz.destiall.sgames.countdown.Countdown;
import xyz.destiall.sgames.match.Match;
import xyz.destiall.sgames.utils.FormatUtils;

public class BossBarModule implements Module, Tickable {
    private final Match match;
    private final String message;
    private Countdown countdown;
    private BossBar bar;

    public BossBarModule(Match match) {
        this.match = match;
        message = SGames.INSTANCE.getConfigManager().getMessage(MessageKey.TIME_REMAINING);
        bar = BossBar.bossBar(Component.text(message), 1.f, BossBar.Color.GREEN, BossBar.Overlay.PROGRESS);
    }

    @Override
    public void load() {
        countdown = match.getCountdown();
    }

    @Override
    public void tick() {
        float progress = 0;
        if (countdown.getDuration().getSeconds() != 0) {
            progress = countdown.getRemaining().getSeconds() / (float) countdown.getDuration().getSeconds();
        }
        String message = this.message;
        if (countdown.getContext() == Countdown.Context.GRACE) {
            message = SGames.INSTANCE.getConfigManager().getMessage(MessageKey.GRACE_PERIOD);
        } else if (countdown.getContext() == Countdown.Context.STARTING) {
            message = SGames.INSTANCE.getConfigManager().getMessage(MessageKey.STARTING);
        }
        bar = bar.name(Component.text(FormatUtils.formatMatch(message, match))).progress(progress)
                 .color(progress > 0.6f ? BossBar.Color.GREEN : progress > 0.3f ? BossBar.Color.YELLOW : BossBar.Color.RED);

        match.forEachPlayer((p) -> p.showBossBar(bar));
    }

    @Override
    public void unload() {
        match.forEachPlayer((p) -> p.hideBossBar(bar));
    }
}
