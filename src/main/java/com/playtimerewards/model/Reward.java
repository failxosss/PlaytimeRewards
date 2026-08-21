package com.playtimerewards.model;

import org.bukkit.Material;

import java.util.List;

/**
 * Represents a single reward (milestone) from the configuration.
 * The reward's key is its time in minutes (time_minutes), which is used
 * to match it against player progress and stored "claimed" data.
 */
public class Reward {

    private final int timeMinutes;
    private final int slot;

    private final Material iconMaterial;
    private final String iconName;
    private final List<String> iconLore;

    private final Material claimedIconMaterial;
    private final String claimedIconName;
    private final List<String> claimedIconLore;

    private final List<String> commands;
    private final String message;
    private final String permission; // may be null / empty = no permission required
    private final boolean broadcast;

    public Reward(int timeMinutes, int slot,
                  Material iconMaterial, String iconName, List<String> iconLore,
                  Material claimedIconMaterial, String claimedIconName, List<String> claimedIconLore,
                  List<String> commands, String message, String permission, boolean broadcast) {
        this.timeMinutes = timeMinutes;
        this.slot = slot;
        this.iconMaterial = iconMaterial;
        this.iconName = iconName;
        this.iconLore = iconLore;
        this.claimedIconMaterial = claimedIconMaterial;
        this.claimedIconName = claimedIconName;
        this.claimedIconLore = claimedIconLore;
        this.commands = commands;
        this.message = message;
        this.permission = permission;
        this.broadcast = broadcast;
    }

    public int getTimeMinutes() {
        return timeMinutes;
    }

    public int getSlot() {
        return slot;
    }

    public Material getIconMaterial() {
        return iconMaterial;
    }

    public String getIconName() {
        return iconName;
    }

    public List<String> getIconLore() {
        return iconLore;
    }

    public Material getClaimedIconMaterial() {
        return claimedIconMaterial;
    }

    public String getClaimedIconName() {
        return claimedIconName;
    }

    public List<String> getClaimedIconLore() {
        return claimedIconLore;
    }

    public List<String> getCommands() {
        return commands;
    }

    public String getMessage() {
        return message;
    }

    public String getPermission() {
        return permission;
    }

    public boolean hasPermission() {
        return permission != null && !permission.isEmpty();
    }

    public boolean isBroadcast() {
        return broadcast;
    }

    /**
     * Returns the milestone time in a human-readable format (e.g. "2h", "30min", "5h").
     */
    public String getFormattedTime() {
        if (timeMinutes < 60) {
            return timeMinutes + "min";
        }
        int hours = timeMinutes / 60;
        int remainder = timeMinutes % 60;
        if (remainder == 0) {
            return hours + "h";
        }
        return hours + "h" + remainder + "min";
    }
}
