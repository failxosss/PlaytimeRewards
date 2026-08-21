package com.playtimerewards.tracker;

import com.playtimerewards.PlaytimeRewards;
import com.playtimerewards.data.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Every minute (of real server run time) checks all online players:
 * - if the player has moved (or been active) since the last check, a minute is added.
 * - if they haven't moved for longer than afk-threshold-seconds, they're considered AFK and time doesn't count.
 *
 * A separate task also runs every X minutes (autosave-interval-minutes)
 * to save changed data to disk.
 */
public class PlaytimeTracker {

    private final PlaytimeRewards plugin;

    // Time (in seconds since server start) when the player was last "active" (moved)
    private final Map<UUID, Long> lastActivitySeconds = new HashMap<>();

    private BukkitTask trackingTask;
    private BukkitTask autosaveTask;
    private long secondsElapsed = 0;

    public PlaytimeTracker(PlaytimeRewards plugin) {
        this.plugin = plugin;
    }

    public void start() {
        // Movement/activity is checked every second (for accurate AFK detection),
        // but playtime only increases once 60 seconds of activity have accumulated.
        trackingTask = new BukkitRunnable() {
            @Override
            public void run() {
                tick();
            }
        }.runTaskTimer(plugin, 20L, 20L); // every second (20 ticks)

        int intervalMinutes = plugin.getConfigManager().getAutosaveIntervalMinutes();
        long intervalTicks = 20L * 60L * Math.max(1, intervalMinutes);
        autosaveTask = new BukkitRunnable() {
            @Override
            public void run() {
                plugin.getPlayerDataManager().saveDirty();
                plugin.getLogger().info("Automatic player data save completed.");
            }
        }.runTaskTimer(plugin, intervalTicks, intervalTicks);
    }

    public void stop() {
        if (trackingTask != null) trackingTask.cancel();
        if (autosaveTask != null) autosaveTask.cancel();
    }

    private final Map<UUID, Integer> secondsAccumulated = new HashMap<>();

    private void tick() {
        secondsElapsed++;
        int afkThreshold = plugin.getConfigManager().getAfkThresholdSeconds();

        for (Player player : Bukkit.getOnlinePlayers()) {
            UUID uuid = player.getUniqueId();
            PlayerData data = plugin.getPlayerDataManager().getData(uuid);

            double x = player.getLocation().getX();
            double y = player.getLocation().getY();
            double z = player.getLocation().getZ();

            boolean moved = Math.abs(x - data.getLastX()) > 0.01
                    || Math.abs(y - data.getLastY()) > 0.01
                    || Math.abs(z - data.getLastZ()) > 0.01;

            if (moved) {
                data.updateLastPosition(x, y, z);
                lastActivitySeconds.put(uuid, secondsElapsed);
            }

            Long lastActive = lastActivitySeconds.getOrDefault(uuid, secondsElapsed);
            boolean isAfk = (secondsElapsed - lastActive) >= afkThreshold;
            data.setAfk(isAfk);

            if (!isAfk) {
                int acc = secondsAccumulated.merge(uuid, 1, Integer::sum);
                if (acc >= 60) {
                    data.addMinute();
                    secondsAccumulated.put(uuid, 0);
                }
            }
        }
    }

    /**
     * Call on player join - sets their initial position so the first
     * second after login isn't automatically counted as movement/activity.
     */
    public void handleJoin(Player player) {
        UUID uuid = player.getUniqueId();
        PlayerData data = plugin.getPlayerDataManager().getData(uuid);
        data.updateLastPosition(player.getLocation().getX(), player.getLocation().getY(), player.getLocation().getZ());
        lastActivitySeconds.put(uuid, secondsElapsed);
        secondsAccumulated.put(uuid, 0);
    }

    /**
     * Call on player quit - cleans up runtime maps and saves data.
     */
    public void handleQuit(Player player) {
        UUID uuid = player.getUniqueId();
        lastActivitySeconds.remove(uuid);
        secondsAccumulated.remove(uuid);
        plugin.getPlayerDataManager().unload(uuid);
    }
}
