package xyz.destiall.sgames.utils;

import org.bukkit.scheduler.BukkitTask;
import xyz.destiall.sgames.SGames;

public class SchedulerUtils {
    private final SGames plugin;
    public SchedulerUtils(SGames plugin) {
        this.plugin = plugin;
    }

    public BukkitTask schedule(Runnable runnable, long seconds) {
        return plugin.getServer().getScheduler().runTaskLater(plugin, runnable, seconds * 20);
    }

    public BukkitTask repeat(Runnable runnable, long period) {
        return plugin.getServer().getScheduler().runTaskTimer(plugin, runnable, 0L, period * 20);
    }

    public BukkitTask scheduleTick(Runnable runnable, long ticks) {
        return plugin.getServer().getScheduler().runTaskLater(plugin, runnable, ticks);
    }

    public BukkitTask repeatTick(Runnable runnable, long periodTicks) {
        return plugin.getServer().getScheduler().runTaskTimer(plugin, runnable, 0L, periodTicks);
    }
}
