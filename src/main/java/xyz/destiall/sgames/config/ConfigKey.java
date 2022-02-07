package xyz.destiall.sgames.config;

public enum ConfigKey {
    CHEST_PROBABILITY("chest.probability", 12),
    CHEST_MAX_CONTENTS("chest.max-contents", 6),
    CHEST_MIN_CONTENTS("chest.min-contents", 3),
    CHEST_KEEP_OPEN("chest.keep-open", false),

    MIN_PLAYERS("players.min-players", 3),
    MAX_PLAYERS("players.max-players", 24),
    KICK_ON_DEATH("players.kick-on-death", false),
    ALLOW_SPECTATOR("players.allow-spectators", false),
    MIN_DEATHMATCH("players.min-deathmatch", 3),

    BORDER_ENABLED("border.enabled", false),
    BORDER_SIZE("border.size", 500),

    DEATH_LIGHTNING("effects.death-lightning", true),
    WIN_FIREWORKS("effects.win-fireworks", true),
    WIN_NIGHT("effects.win-set-night", true),
    DEATHMATCH_NIGHT("effects.dm-set-night", true),

    DURATION_STARTING("duration.starting", "20s"),
    DURATION_GRACE("duration.grace-period", "0s"),
    DURATION_RUNNING("duration.running", "30m"),
    DURATION_DEATHMATCH("duration.deathmatch", "10m"),
    DURATION_FINISHING("duration.finishing", "5s"),
    DURATION_UNKNOWN("duration.unknown", "0s"),

    RANDOM_SPAWNPOINT("spawn.random", false),

    SETUP_MODE("setup-mode", true),

    ;
    public final String key;
    public final Object def;
    ConfigKey(String key, Object def) {
        this.key = key;
        this.def = def;
    }
}
