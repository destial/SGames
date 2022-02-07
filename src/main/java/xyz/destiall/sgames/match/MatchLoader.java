package xyz.destiall.sgames.match;

import org.bukkit.scheduler.BukkitRunnable;

public class MatchLoader extends BukkitRunnable {
    private final MatchFactory factory;
    public MatchLoader(MatchFactory factory) {
        this.factory = factory;
    }

    @Override
    public void run() {
        factory.await();
    }
}
