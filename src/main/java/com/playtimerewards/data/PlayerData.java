package com.playtimerewards.data;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Holds both runtime and persistent data for a single player:
 * - measured playtime in minutes
 * - the set of claimed reward keys (time_minutes)
 * - helper AFK data (last position and last-active timestamp)
 */
public class PlayerData {

    private final UUID uuid;
    private long playtimeMinutes;
    private final Set<Integer> claimedRewards;

    // AFK detection - not saved to file, runtime only
    private transient double lastX;
    private transient double lastY;
    private transient double lastZ;
    private transient boolean afk;
    private transient boolean dirty; // true = needs to be saved to disk

    public PlayerData(UUID uuid) {
        this.uuid = uuid;
        this.playtimeMinutes = 0;
        this.claimedRewards = new HashSet<>();
        this.afk = false;
        this.dirty = false;
    }

    public UUID getUuid() {
        return uuid;
    }

    public long getPlaytimeMinutes() {
        return playtimeMinutes;
    }

    public void setPlaytimeMinutes(long playtimeMinutes) {
        this.playtimeMinutes = playtimeMinutes;
    }

    public void addMinute() {
        this.playtimeMinutes++;
        this.dirty = true;
    }

    public void addMinutes(long amount) {
        this.playtimeMinutes += amount;
        this.dirty = true;
    }

    public Set<Integer> getClaimedRewards() {
        return claimedRewards;
    }

    public boolean hasClaimed(int rewardKey) {
        return claimedRewards.contains(rewardKey);
    }

    public void claim(int rewardKey) {
        claimedRewards.add(rewardKey);
        this.dirty = true;
    }

    public boolean isAfk() {
        return afk;
    }

    public void setAfk(boolean afk) {
        this.afk = afk;
    }

    public double getLastX() {
        return lastX;
    }

    public double getLastY() {
        return lastY;
    }

    public double getLastZ() {
        return lastZ;
    }

    public void updateLastPosition(double x, double y, double z) {
        this.lastX = x;
        this.lastY = y;
        this.lastZ = z;
    }

    public boolean isDirty() {
        return dirty;
    }

    public void markSaved() {
        this.dirty = false;
    }

    public void markDirty() {
        this.dirty = true;
    }
}
