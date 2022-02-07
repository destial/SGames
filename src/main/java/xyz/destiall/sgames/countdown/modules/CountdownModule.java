package xyz.destiall.sgames.countdown.modules;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Sound;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import xyz.destiall.sgames.SGames;
import xyz.destiall.sgames.api.Module;
import xyz.destiall.sgames.config.ConfigKey;
import xyz.destiall.sgames.config.MessageKey;
import xyz.destiall.sgames.countdown.Countdown;
import xyz.destiall.sgames.countdown.events.CountdownCallEvent;
import xyz.destiall.sgames.countdown.events.CountdownEndEvent;
import xyz.destiall.sgames.manager.ConfigManager;
import xyz.destiall.sgames.match.Match;
import xyz.destiall.sgames.match.events.GraceEndEvent;
import xyz.destiall.sgames.match.events.MatchStartEvent;
import xyz.destiall.sgames.utils.SoundUtils;
import xyz.destiall.sgames.utils.TextColorUtils;

public class CountdownModule implements Module, Listener {
    private final Match match;
    public CountdownModule(Match match) {
        this.match = match;
    }

    @EventHandler
    public void onCountdownEnd(CountdownEndEvent e) {
        if (e.getCountdown() != match.getCountdown()) return;
        Countdown.Context newContext;
        ConfigManager cm = SGames.INSTANCE.getConfigManager();
        switch (e.getContext()) {
            case STARTING:
                newContext = !cm.getDuration(ConfigKey.DURATION_GRACE).isZero() ? Countdown.Context.GRACE : Countdown.Context.RUNNING;
                match.callEvent(new MatchStartEvent(match));
                break;
            case GRACE:
                newContext = Countdown.Context.RUNNING;
                match.callEvent(new GraceEndEvent(match));
                break;
            case RUNNING:
                match.teleport();
                newContext = Countdown.Context.STARTING_DM;
                break;
            case STARTING_DM:
                newContext = Countdown.Context.DEATHMATCH;
                break;
            case DEATHMATCH:
                match.calculateWinConditions();
                newContext = Countdown.Context.FINISHING;
                break;
            default: newContext = null;
        }

        if (newContext == null) {
            SGames.INSTANCE.getServer().shutdown();
            return;
        }

        e.getCountdown().setContext(newContext);
    }

    @EventHandler
    public void onMatchStart(MatchStartEvent e) {
        match.broadcast(SGames.INSTANCE.getConfigManager().getMessage(MessageKey.START));
        match.forEachPlayer((p) -> p.playSound(SoundUtils.of(Sound.ENTITY_WITHER_SPAWN, 1, 1)));
    }

    @EventHandler
    public void onGraceEnd(GraceEndEvent e) {
        match.broadcast(SGames.INSTANCE.getConfigManager().getMessage(MessageKey.GRACE_PERIOD_END));
    }

    @EventHandler
    public void onCountdownCall(CountdownCallEvent e) {
        if (e.getRemaining().getSeconds() <= 5) {
            long seconds = e.getRemaining().getSeconds();
            if (e.getContext() == Countdown.Context.STARTING || e.getContext() == Countdown.Context.STARTING_DM) {
                TextColor color = seconds <= 3 ? TextColorUtils.RED : TextColorUtils.GREEN;
                Component title = Component.text(seconds, color);
                match.sendTitle(title);
            }
        }
    }
}
