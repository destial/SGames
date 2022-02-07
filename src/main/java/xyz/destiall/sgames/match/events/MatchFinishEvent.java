package xyz.destiall.sgames.match.events;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import xyz.destiall.sgames.match.Match;
import xyz.destiall.sgames.player.Competitor;

public class MatchFinishEvent extends Event {
    private final Match match;
    public MatchFinishEvent(Match match) {
        this.match = match;
    }

    public Match getMatch() {
        return match;
    }

    public Competitor getWinner() {
        return match.getWinner();
    }

    private static final HandlerList handlers=new HandlerList();
    @Override public HandlerList getHandlers(){return handlers;}
    public static HandlerList getHandlerList(){return handlers;}
}
