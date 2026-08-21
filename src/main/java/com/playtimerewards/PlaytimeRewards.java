package com.playtimerewards;

import com.playtimerewards.commands.PlaytimeCommand;
import com.playtimerewards.config.ConfigManager;
import com.playtimerewards.data.PlayerDataManager;
import com.playtimerewards.gui.GUIListener;
import com.playtimerewards.gui.RewardsGUI;
import com.playtimerewards.tracker.PlaytimeTracker;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Main class of the PlaytimeRewards plugin.
 * Handles initializing all managers, registering commands/listeners,
 * and a clean shutdown (saving data) when the server stops.
 */
public final class PlaytimeRewards extends JavaPlugin {

    private ConfigManager configManager;
    private PlayerDataManager playerDataManager;
    private PlaytimeTracker playtimeTracker;
    private RewardsGUI guiManager;

    @Override
    public void onEnable() {
        // 1. Configuration
        this.configManager = new ConfigManager(this);
        this.configManager.loadAll();

        // 2. Player data
        this.playerDataManager = new PlayerDataManager(this);

        // 3. GUI
        this.guiManager = new RewardsGUI(this);

        // 4. Playtime tracking
        this.playtimeTracker = new PlaytimeTracker(this);
        this.playtimeTracker.start();

        // 5. Listener
        getServer().getPluginManager().registerEvents(new GUIListener(this), this);

        // 6. Commands
        PlaytimeCommand executor = new PlaytimeCommand(this);
        var command = getCommand("playtimerewards");
        if (command != null) {
            command.setExecutor(executor);
            command.setTabCompleter(executor);
        }

        getLogger().info("PlaytimeRewards has been enabled. Loaded " + configManager.getRewards().size() + " rewards.");
    }

    @Override
    public void onDisable() {
        if (playtimeTracker != null) {
            playtimeTracker.stop();
        }
        if (playerDataManager != null) {
            playerDataManager.saveAll();
        }
        getLogger().info("PlaytimeRewards has been disabled, player data has been saved.");
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public PlayerDataManager getPlayerDataManager() {
        return playerDataManager;
    }

    public PlaytimeTracker getPlaytimeTracker() {
        return playtimeTracker;
    }

    public RewardsGUI getGuiManager() {
        return guiManager;
    }
}
