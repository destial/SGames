package xyz.destiall.sgames.config;

public enum MessageKey {
    LOBBY_READY("messages.ready", "&aThe game is ready!"),
    KILL("messages.kill", "&6A cannon could be heard in the distance."),
    DIED("messages.died", "&cYou died!"),
    STARTING("messages.starting" ,"&aThe game is starting in {time}"),
    START("messages.start", "&aThe game has started! Good luck!"),
    GRACE_PERIOD("messages.grace-period", "&bGrace period will end in {time}"),
    GRACE_PERIOD_END("messages.grace-period-end", "&bGrace period has ended! PvP is now enabled!"),
    UNTIL_DEATHMATCH("messages.until-deathmatch", "&cDeathmatch will start in {time}"),
    DEATHMATCH("messages.deathmatch", "&cDeathmatch will end in {time}"),
    FINISH("messages.finish", "&cThe game has finished! Returning in {time}"),
    GAME_STARTED("messages.game-started", "&cThe game has already started!"),
    GAME_FULL("messages.full", "&cThe game is already full!"),
    SPECTATE_PREFIX("messages.spectate-prefix", "&7[SPECTATING] "),
    TIME_REMAINING("messages.time-remaining", "&aTime Remaining: &e{time}"),
    WINNER("messages.winner", "&a{winner} has won the &6Hunger Games!"),
    UNKNOWN("messages.unknown", "Unknown"),

    UNIT_SECOND("messages.duration.second", "s"),
    UNIT_MINUTE("messages.duration.minute", "m"),
    UNIT_HOUR("messages.duration.hour", "h"),

    ;
    public final String key;
    public final String def;
    MessageKey(String key, String def) {
        this.key = key;
        this.def = def;
    }
}
