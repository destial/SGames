package xyz.destiall.sgames.listeners;

import net.kyori.adventure.sound.Sound;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import xyz.destiall.sgames.SGames;
import xyz.destiall.sgames.config.MessageKey;
import xyz.destiall.sgames.countdown.events.CountdownCallEvent;
import xyz.destiall.sgames.lobby.events.ReadyToStartEvent;
import xyz.destiall.sgames.match.Match;
import xyz.destiall.sgames.utils.SoundUtils;

public class MatchListener implements Listener {
    private final SGames plugin;
    public MatchListener(SGames plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onCountdownCall(CountdownCallEvent e) {
        Match match = plugin.getMatchManager().getMatch();
        if (match != null) {
            match.broadcast(e.getMessage());
            Sound sound = SoundUtils.of(org.bukkit.Sound.BLOCK_LEVER_CLICK, 1, 1);
            match.forEachPlayer((p) -> p.playSound(sound));
        } else {
            plugin.getServer().getConsoleSender().sendMessage(e.getMessage());
            for (Player player : Bukkit.getOnlinePlayers()) {
                player.sendMessage(e.getMessage());
                player.playSound(player.getLocation(), org.bukkit.Sound.BLOCK_LEVER_CLICK, 1, 1);
            }
        }
    }

    @EventHandler
    public void onReady(ReadyToStartEvent e) {
        String message = SGames.INSTANCE.getConfigManager().getMessage(MessageKey.LOBBY_READY);
        plugin.getServer().getConsoleSender().sendMessage(message);
        for (Player player : e.getLobby().queue()) {
            player.sendMessage(message);
            player.playSound(player.getLocation(), org.bukkit.Sound.BLOCK_LEVER_CLICK, 1, 1);
        }
    }
}
