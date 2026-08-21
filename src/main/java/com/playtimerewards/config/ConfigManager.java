package com.playtimerewards.config;

import com.playtimerewards.PlaytimeRewards;
import com.playtimerewards.model.Reward;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;

/**
 * Handles loading config.yml (settings + rewards) and messages.yml (texts).
 * Rewards are fully configurable - adding/editing/removing them in config.yml
 * takes effect after /pt reload without touching any code.
 */
public class ConfigManager {

    private final PlaytimeRewards plugin;

    private FileConfiguration config;
    private FileConfiguration messages;

    // Rewards sorted by time_minutes (ascending) - key = time_minutes
    private final Map<Integer, Reward> rewards = new TreeMap<>();

    // General settings
    private String guiTitle;
    private Material backgroundMaterial;
    private int afkThresholdSeconds;
    private int autosaveIntervalMinutes;
    private boolean soundOnClaim;

    public ConfigManager(PlaytimeRewards plugin) {
        this.plugin = plugin;
    }

    public void loadAll() {
        saveDefaultResource("config.yml");
        saveDefaultResource("messages.yml");

        this.config = YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder(), "config.yml"));
        this.messages = YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder(), "messages.yml"));

        loadSettings();
        loadRewards();
    }

    /**
     * Copies the default file from resources into the plugin's data folder if it isn't there yet.
     */
    private void saveDefaultResource(String name) {
        File target = new File(plugin.getDataFolder(), name);
        if (!target.exists()) {
            plugin.saveResource(name, false);
        }
    }

    private void loadSettings() {
        guiTitle = color(config.getString("gui.title", "&6&lPlaytime Rewards"));
        String bgMat = config.getString("gui.background-material", "GRAY_STAINED_GLASS_PANE");
        backgroundMaterial = Material.matchMaterial(bgMat);
        if (backgroundMaterial == null) {
            backgroundMaterial = Material.GRAY_STAINED_GLASS_PANE;
        }

        afkThresholdSeconds = config.getInt("settings.afk-threshold-seconds", 120);
        autosaveIntervalMinutes = config.getInt("settings.autosave-interval-minutes", 5);
        soundOnClaim = config.getBoolean("settings.sound-on-claim", true);
    }

    private void loadRewards() {
        rewards.clear();
        ConfigurationSection section = config.getConfigurationSection("rewards");
        if (section == null) {
            plugin.getLogger().warning("Section 'rewards' is missing in config.yml - no rewards will be available.");
            return;
        }

        for (String key : section.getKeys(false)) {
            ConfigurationSection rewardSection = section.getConfigurationSection(key);
            if (rewardSection == null) continue;

            try {
                int timeMinutes = rewardSection.getInt("time_minutes");
                int slot = rewardSection.getInt("slot");

                ConfigurationSection iconSec = rewardSection.getConfigurationSection("icon");
                Material iconMat = Material.matchMaterial(
                        iconSec != null ? iconSec.getString("material", "WHITE_CANDLE") : "WHITE_CANDLE");
                if (iconMat == null) iconMat = Material.WHITE_CANDLE;
                String iconName = color(iconSec != null ? iconSec.getString("name", "&fReward") : "&fReward");
                List<String> iconLore = colorList(iconSec != null ? iconSec.getStringList("lore") : new ArrayList<>());

                ConfigurationSection claimedSec = rewardSection.getConfigurationSection("claimed_icon");
                Material claimedMat = Material.matchMaterial(
                        claimedSec != null ? claimedSec.getString("material", "GRAY_CANDLE") : "GRAY_CANDLE");
                if (claimedMat == null) claimedMat = Material.GRAY_CANDLE;
                String claimedName = color(claimedSec != null ? claimedSec.getString("name", "&7Claimed") : "&7Claimed");
                List<String> claimedLore = colorList(claimedSec != null ? claimedSec.getStringList("lore") : new ArrayList<>());

                List<String> commands = rewardSection.getStringList("commands");
                String message = color(rewardSection.getString("message", ""));
                String permission = rewardSection.getString("permission", "");
                boolean broadcast = rewardSection.getBoolean("broadcast", false);

                Reward reward = new Reward(timeMinutes, slot, iconMat, iconName, iconLore,
                        claimedMat, claimedName, claimedLore, commands, message, permission, broadcast);

                rewards.put(timeMinutes, reward);
            } catch (Exception e) {
                plugin.getLogger().log(Level.WARNING, "Error loading reward '" + key + "' from config.yml", e);
            }
        }
    }

    public void reload() {
        this.config = YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder(), "config.yml"));
        this.messages = YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder(), "messages.yml"));
        loadSettings();
        loadRewards();
    }

    // ---------- getters ----------

    public Map<Integer, Reward> getRewards() {
        return rewards;
    }

    public String getGuiTitle() {
        return guiTitle;
    }

    public Material getBackgroundMaterial() {
        return backgroundMaterial;
    }

    public int getAfkThresholdSeconds() {
        return afkThresholdSeconds;
    }

    public int getAutosaveIntervalMinutes() {
        return autosaveIntervalMinutes;
    }

    public boolean isSoundOnClaim() {
        return soundOnClaim;
    }

    /**
     * Returns a message from messages.yml, replaces %player% with the player's
     * name and %prefix% with the prefix, and converts & codes into colors.
     */
    public String getMessage(String path, String playerName) {
        String raw = messages.getString(path, path);
        String prefix = messages.getString("prefix", "&8[&6PlaytimeRewards&8] &r");
        raw = raw.replace("%prefix%", prefix);
        if (playerName != null) {
            raw = raw.replace("%player%", playerName);
        }
        return color(raw);
    }

    public String getMessageRaw(String path) {
        return messages.getString(path, path);
    }

    // ---------- helper methods ----------

    public static String color(String input) {
        if (input == null) return "";
        return input.replace('&', '§');
    }

    public static List<String> colorList(List<String> input) {
        List<String> result = new ArrayList<>();
        for (String line : input) {
            result.add(color(line));
        }
        return result;
    }
}
