package xyz.destiall.sgames.player.events;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import xyz.destiall.sgames.api.Killer;
import xyz.destiall.sgames.match.Match;
import xyz.destiall.sgames.player.Competitor;

public class CompetitorDeathEvent extends Event {
    private final Competitor competitor;
    private final Killer killer;

    public CompetitorDeathEvent(Competitor competitor, Killer killer) {
        this.competitor = competitor;
        this.killer = killer;
    }

    public Competitor getCompetitor() {
        return competitor;
    }

    public Killer getKiller() {
        return killer;
    }

    public boolean isQuit() {
        return killer.isQuit();
    }

    public Match getMatch() {
        return competitor.getMatch();
    }

    private static final HandlerList handlers=new HandlerList();
    @Override public HandlerList getHandlers(){return handlers;}
    public static HandlerList getHandlerList(){return handlers;}
}
