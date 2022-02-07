package xyz.destiall.sgames.match.events;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import xyz.destiall.sgames.lobby.Lobby;
import xyz.destiall.sgames.map.MapInfo;

public class MatchInitEvent extends Event {
    private final Lobby lobby;
    private final MapInfo map;
    public MatchInitEvent(Lobby lobby, MapInfo map) {
        this.lobby = lobby;
        this.map = map;
    }

    public MapInfo getMap() {
        return map;
    }

    public Lobby getLobby() {
        return lobby;
    }

    private static final HandlerList handlers=new HandlerList();
    @Override public HandlerList getHandlers(){return handlers;}
    public static HandlerList getHandlerList(){return handlers;}
}
