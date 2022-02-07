package xyz.destiall.sgames.lobby.events;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import xyz.destiall.sgames.lobby.Lobby;

public class ReadyToStartEvent extends Event {
    private final Lobby lobby;
    public ReadyToStartEvent(Lobby lobby) {
        this.lobby = lobby;
    }

    public Lobby getLobby() {
        return lobby;
    }

    private static final HandlerList handlers=new HandlerList();
    @Override public HandlerList getHandlers(){return handlers;}
    public static HandlerList getHandlerList(){return handlers;}
}
