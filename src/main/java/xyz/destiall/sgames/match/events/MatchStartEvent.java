package xyz.destiall.sgames.match.events;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import xyz.destiall.sgames.match.Match;

public class MatchStartEvent extends Event {
    private final Match match;
    public MatchStartEvent(Match match) {
        this.match = match;
    }

    public Match getMatch() {
        return match;
    }

    private static final HandlerList handlers=new HandlerList();
    @Override public HandlerList getHandlers(){return handlers;}
    public static HandlerList getHandlerList(){return handlers;}
}
