package xyz.destiall.sgames.countdown;

import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import xyz.destiall.sgames.SGames;
import xyz.destiall.sgames.api.Tickable;
import xyz.destiall.sgames.config.ConfigKey;
import xyz.destiall.sgames.config.MessageKey;
import xyz.destiall.sgames.countdown.events.CountdownCallEvent;
import xyz.destiall.sgames.countdown.events.CountdownEndEvent;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

public class Countdown implements Tickable {
    private Duration duration;
    private Duration remaining;
    private Context context;

    public Countdown() {
        setContext(Context.STARTING);
    }

    public void setContext(Context context) {
        this.context = context;
        this.duration = SGames.INSTANCE.getConfigManager().getDuration(context.durationKey);
        this.remaining = Duration.of(duration.getSeconds(), ChronoUnit.SECONDS);
    }

    public void setDuration(Duration duration) {
        if (duration.getSeconds() < this.remaining.getSeconds()) {
            this.duration = duration;
            this.remaining = Duration.of(duration.getSeconds(), ChronoUnit.SECONDS);
        }
    }

    public Context getContext() {
        return context;
    }

    public Duration getDuration() {
        return duration;
    }

    public Duration getRemaining() {
        return remaining;
    }

    @Override
    public void tick() {
        if (remaining.isZero() || remaining.isNegative()) return;

        remaining = remaining.minusSeconds(1);
        long time = remaining.getSeconds();

        String message = SGames.INSTANCE.getConfigManager().getMessage(context.messageKey);
        message = message.replace("{time}", getFormattedDuration());
        if (time == 0) {
            callEvent(new CountdownEndEvent(this));
        } else if (time <= 30 && time % 5 == 0) {
            callEvent(new CountdownCallEvent(this, message));
        } else if (time < 10) {
            callEvent(new CountdownCallEvent(this, message));
        } else if (time >= 600 && time % 600 == 0) {
            callEvent(new CountdownCallEvent(this, message));
        } else if (time < 600 && time % 300 == 0) {
            callEvent(new CountdownCallEvent(this, message));
        } else if (time == 60) {
            callEvent(new CountdownCallEvent(this, message));
        }
    }

    public void callEvent(Event event) {
        Bukkit.getPluginManager().callEvent(event);
    }

    public String getFormattedDuration(Duration duration) {
        long convertedTime = duration.getSeconds();
        String unit = SGames.INSTANCE.getConfigManager().getMessage(MessageKey.UNIT_SECOND);
        if (convertedTime >= 3600) {
            unit = SGames.INSTANCE.getConfigManager().getMessage(MessageKey.UNIT_HOUR);
            convertedTime = convertedTime/3600;
        } else if (convertedTime >= 60) {
            unit = SGames.INSTANCE.getConfigManager().getMessage(MessageKey.UNIT_MINUTE);
            convertedTime = convertedTime/60;
        }
        return "" + convertedTime + unit;
    }

    public String getFormattedDuration() {
        return getFormattedDuration(remaining);
    }

    public enum Context {
        UNKNOWN(MessageKey.UNKNOWN, ConfigKey.DURATION_UNKNOWN),
        STARTING(MessageKey.STARTING, ConfigKey.DURATION_STARTING),
        GRACE(MessageKey.GRACE_PERIOD, ConfigKey.DURATION_GRACE),
        RUNNING(MessageKey.UNTIL_DEATHMATCH, ConfigKey.DURATION_RUNNING),
        STARTING_DM(MessageKey.UNTIL_DEATHMATCH, ConfigKey.DURATION_GRACE),
        DEATHMATCH(MessageKey.DEATHMATCH, ConfigKey.DURATION_DEATHMATCH),
        FINISHING(MessageKey.FINISH, ConfigKey.DURATION_FINISHING),

        ;
        private final MessageKey messageKey;
        private final ConfigKey durationKey;
        Context(MessageKey messageKey, ConfigKey durationKey) {
            this.messageKey = messageKey;
            this.durationKey = durationKey;
        }
    }
}
