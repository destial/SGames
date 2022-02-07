package xyz.destiall.sgames.api;

import java.util.UUID;

public abstract class Stats {
    private int kills;
    private int losses;
    private int wins;

    private final UUID uuid;

    public Stats(UUID uuid) {
        this.uuid = uuid;
    }

    public UUID getUuid() {
        return uuid;
    }

    public int getKills() {
        return kills;
    }

    public int getWins() {
        return wins;
    }

    public int getLosses() {
        return losses;
    }

    public void setKills(int kills) {
        this.kills = kills;
    }

    public void setLosses(int losses) {
        this.losses = losses;
    }

    public void setWins(int wins) {
        this.wins = wins;
    }

    public void addLosses() {
        this.losses++;
    }

    public void addKill() {
        this.kills++;
    }

    public void addWin() {
        this.wins++;
    }
}
