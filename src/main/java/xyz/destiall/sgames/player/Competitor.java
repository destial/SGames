package xyz.destiall.sgames.player;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.PlayerInventory;
import xyz.destiall.sgames.api.SGPlayer;
import xyz.destiall.sgames.map.SpawnPoint;
import xyz.destiall.sgames.match.Match;
import xyz.destiall.sgames.player.modules.ScoreboardModule;

public class Competitor extends SGPlayer {
    private final SpawnPoint spawnPoint;
    private Location lastSeen;
    private PlayerInventory inventory;

    public Competitor(Player player, Match match, SpawnPoint point) {
        super(player, match);
        this.spawnPoint = point;
    }

    public void teleportToSpawnPoint() {
        spawnPoint.getLocation().setWorld(match.getWorld());
        teleport(spawnPoint.getLocation());
        setGameMode(GameMode.SURVIVAL);
    }

    public SpawnPoint getSpawnPoint() {
        return spawnPoint;
    }

    public void quit() {
        if (player == null) return;
        lastSeen = player.getLocation();
        inventory = player.getInventory();
        super.setBukkit(null);
    }

    @Override
    public void setBukkit(Player player) {
        super.setBukkit(player);
        if (player != null) {
            player.teleport(lastSeen);
            player.getInventory().setContents(inventory.getContents());
            player.getInventory().setArmorContents(inventory.getArmorContents());
            player.getInventory().setExtraContents(inventory.getExtraContents());
            ScoreboardModule sm = match.getModule(ScoreboardModule.class);
            sm.addPlayer(player);
        }
    }

    @Override
    public PlayerInventory getInventory() {
        return player != null ? player.getInventory() : inventory;
    }
}
