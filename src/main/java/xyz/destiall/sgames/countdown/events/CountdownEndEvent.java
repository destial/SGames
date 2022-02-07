package xyz.destiall.sgames.countdown.events;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import xyz.destiall.sgames.countdown.Countdown;

public class CountdownEndEvent extends Event {
    private final Countdown countdown;
    private final Countdown.Context context;

    public CountdownEndEvent(Countdown countdown) {
        this.countdown = countdown;
        this.context = countdown.getContext();
    }

    public Countdown.Context getContext() {
        return context;
    }

    public Countdown getCountdown() {
        return countdown;
    }

    private static final HandlerList handlers=new HandlerList();
    @Override public HandlerList getHandlers(){return handlers;}
    public static HandlerList getHandlerList(){return handlers;}
}
