package com.playtimerewards.gui;

import com.playtimerewards.PlaytimeRewards;
import com.playtimerewards.data.PlayerData;
import com.playtimerewards.model.Reward;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * Listens for clicks in the rewards GUI and for the basic player events
 * needed for playtime tracking (join/quit/move).
 */
public class GUIListener implements Listener {

    private final PlaytimeRewards plugin;

    public GUIListener(PlaytimeRewards plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getInventory().getHolder() instanceof RewardsHolder)) {
            return;
        }
        event.setCancelled(true);

        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        int slot = event.getRawSlot();
        Reward reward = findRewardBySlot(slot);
        if (reward == null) {
            return; // click on background or outside a defined reward
        }

        handleClaim(player, reward);
    }

    private Reward findRewardBySlot(int slot) {
        for (Reward reward : plugin.getConfigManager().getRewards().values()) {
            if (reward.getSlot() == slot) {
                return reward;
            }
        }
        return null;
    }

    private void handleClaim(Player player, Reward reward) {
        PlayerData data = plugin.getPlayerDataManager().getData(player.getUniqueId());

        if (data.hasClaimed(reward.getTimeMinutes())) {
            player.sendMessage(plugin.getConfigManager().getMessage("gui.already-claimed", player.getName()));
            return;
        }

        if (reward.hasPermission() && !player.hasPermission(reward.getPermission())) {
            player.sendMessage(plugin.getConfigManager().getMessage("gui.no-permission", player.getName()));
            return;
        }

        if (data.getPlaytimeMinutes() < reward.getTimeMinutes()) {
            player.sendMessage(plugin.getConfigManager().getMessage("gui.not-ready", player.getName()));
            return;
        }

        // Everything checks out - claim the reward
        data.claim(reward.getTimeMinutes());

        // Save immediately (not just on autosave/disconnect) so the reward
        // can't be claimed twice even after a hard server crash right after claiming.
        plugin.getPlayerDataManager().save(data);

        // Run the configured commands as the console, with %player% substituted
        for (String cmd : reward.getCommands()) {
            String parsed = cmd.replace("%player%", player.getName());
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), parsed);
        }

        // Message to the player (if defined on the reward)
        if (reward.getMessage() != null && !reward.getMessage().isEmpty()) {
            player.sendMessage(reward.getMessage()
                    .replace("%player%", player.getName())
                    .replace("%time%", reward.getFormattedTime()));
        }

        if (reward.isBroadcast()) {
            String broadcastMsg = plugin.getConfigManager().getMessage("gui.broadcast-claim", player.getName())
                    .replace("%time%", reward.getFormattedTime());
            Bukkit.broadcastMessage(broadcastMsg);
        }

        if (plugin.getConfigManager().isSoundOnClaim()) {
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
        }

        // Re-render the GUI so the icon immediately switches to "claimed"
        player.openInventory(plugin.getGuiManager().build(player));
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        plugin.getPlaytimeTracker().handleJoin(event.getPlayer());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        plugin.getPlaytimeTracker().handleQuit(event.getPlayer());
    }
}
