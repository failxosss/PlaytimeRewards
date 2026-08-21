package com.playtimerewards.data;

import com.playtimerewards.PlaytimeRewards;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

/**
 * Handles loading and saving player data to /plugins/PlaytimeRewards/playerdata/<uuid>.yml
 * In-memory data is kept in a ConcurrentHashMap, since it can be accessed
 * both from the main thread (GUI, commands) and from async tasks (saving).
 */
public class PlayerDataManager {

    private final PlaytimeRewards plugin;
    private final File dataFolder;
    private final Map<UUID, PlayerData> cache = new ConcurrentHashMap<>();

    public PlayerDataManager(PlaytimeRewards plugin) {
        this.plugin = plugin;
        this.dataFolder = new File(plugin.getDataFolder(), "playerdata");
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }
    }

    /**
     * Returns the player's data from cache; if not present, loads it from disk (or creates new data).
     */
    public PlayerData getData(UUID uuid) {
        return cache.computeIfAbsent(uuid, this::loadFromDisk);
    }

    public boolean isLoaded(UUID uuid) {
        return cache.containsKey(uuid);
    }

    public void unload(UUID uuid) {
        PlayerData data = cache.remove(uuid);
        if (data != null) {
            save(data);
        }
    }

    private PlayerData loadFromDisk(UUID uuid) {
        File file = new File(dataFolder, uuid.toString() + ".yml");
        PlayerData data = new PlayerData(uuid);

        if (file.exists()) {
            YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
            data.setPlaytimeMinutes(yaml.getLong("playtime-minutes", 0));
            List<Integer> claimed = yaml.getIntegerList("claimed-rewards");
            data.getClaimedRewards().addAll(claimed);
        }

        return data;
    }

    /**
     * Saves a specific PlayerData to disk (called periodically and on player quit).
     */
    public void save(PlayerData data) {
        File file = new File(dataFolder, data.getUuid().toString() + ".yml");
        YamlConfiguration yaml = new YamlConfiguration();
        yaml.set("playtime-minutes", data.getPlaytimeMinutes());
        yaml.set("claimed-rewards", data.getClaimedRewards().stream().toList());

        try {
            yaml.save(file);
            data.markSaved();
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to save data for player " + data.getUuid(), e);
        }
    }

    /**
     * Saves all currently loaded player data (called by the periodic task and on onDisable).
     */
    public void saveAll() {
        for (PlayerData data : cache.values()) {
            save(data);
        }
    }

    /**
     * Saves only data that has changed since the last save.
     */
    public void saveDirty() {
        for (PlayerData data : cache.values()) {
            if (data.isDirty()) {
                save(data);
            }
        }
    }

    /**
     * Fully resets a player's data (/pt reset command).
     */
    public void reset(UUID uuid) {
        PlayerData fresh = new PlayerData(uuid);
        cache.put(uuid, fresh);
        save(fresh);
    }
}
