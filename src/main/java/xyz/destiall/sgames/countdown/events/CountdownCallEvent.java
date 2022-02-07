package xyz.destiall.sgames.countdown.events;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import xyz.destiall.sgames.countdown.Countdown;

import java.time.Duration;

public class CountdownCallEvent extends Event {
    private final Countdown countdown;
    private final Countdown.Context context;
    private final String message;

    public CountdownCallEvent(Countdown countdown, String message) {
        this.countdown = countdown;
        this.message = message;
        this.context = countdown.getContext();
    }

    public Duration getRemaining() {
        return countdown.getRemaining();
    }

    public Countdown.Context getContext() {
        return context;
    }

    public Countdown getCountdown() {
        return countdown;
    }

    public String getMessage() {
        return message;
    }

    private static final HandlerList handlers=new HandlerList();
    @Override public HandlerList getHandlers(){return handlers;}
    public static HandlerList getHandlerList(){return handlers;}
}
