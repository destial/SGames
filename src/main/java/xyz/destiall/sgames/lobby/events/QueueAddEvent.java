package xyz.destiall.sgames.lobby.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import xyz.destiall.sgames.lobby.Lobby;

public class QueueAddEvent extends Event {
    private final Lobby lobby;
    private final Player player;

    public QueueAddEvent(Lobby lobby, Player player) {
        this.lobby = lobby;
        this.player = player;
    }

    public Player getPlayer() {
        return player;
    }

    public Lobby getLobby() {
        return lobby;
    }

    private static final HandlerList handlers=new HandlerList();
    @Override public HandlerList getHandlers(){return handlers;}
    public static HandlerList getHandlerList(){return handlers;}
}
