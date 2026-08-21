package com.playtimerewards.gui;

import com.playtimerewards.PlaytimeRewards;
import com.playtimerewards.data.PlayerData;
import com.playtimerewards.model.Reward;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Builds a 6x9 (54-slot) GUI inventory with 4 rows of 4 candles (16 rewards)
 * surrounded by a dark glass background.
 *
 * Each reward's state is evaluated in real time based on PlayerData:
 * - Claimed (claimed_icon, green status "✔ Claimed")
 * - Ready to claim (yellow status "⏳ Ready") - player has enough playtime but hasn't clicked yet
 * - Locked (red status "✘ Locked") - player doesn't have enough playtime yet
 */
public class RewardsGUI {

    public static final int SIZE = 54;

    private final PlaytimeRewards plugin;

    public RewardsGUI(PlaytimeRewards plugin) {
        this.plugin = plugin;
    }

    public Inventory build(Player player) {
        String title = plugin.getConfigManager().getGuiTitle();
        Inventory inventory = Bukkit.createInventory(new RewardsHolder(), SIZE, title);

        // Background - dark glass panes on every slot
        ItemStack background = createBackgroundItem();
        for (int i = 0; i < SIZE; i++) {
            inventory.setItem(i, background);
        }

        // Rewards
        PlayerData data = plugin.getPlayerDataManager().getData(player.getUniqueId());
        Map<Integer, Reward> rewards = plugin.getConfigManager().getRewards();

        for (Reward reward : rewards.values()) {
            if (reward.getSlot() < 0 || reward.getSlot() >= SIZE) continue;
            ItemStack item = buildRewardItem(player, data, reward);
            inventory.setItem(reward.getSlot(), item);
        }

        return inventory;
    }

    private ItemStack createBackgroundItem() {
        ItemStack item = new ItemStack(plugin.getConfigManager().getBackgroundMaterial());
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(" ");
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack buildRewardItem(Player player, PlayerData data, Reward reward) {
        boolean claimed = data.hasClaimed(reward.getTimeMinutes());
        boolean unlocked = data.getPlaytimeMinutes() >= reward.getTimeMinutes();
        boolean hasPermission = !reward.hasPermission() || player.hasPermission(reward.getPermission());

        ItemStack item;
        String statusLine;

        if (claimed) {
            item = new ItemStack(reward.getClaimedIconMaterial());
            statusLine = "§a✔ Claimed";
        } else if (unlocked && hasPermission) {
            item = new ItemStack(reward.getIconMaterial());
            statusLine = "§e⏳ Ready";
        } else {
            item = new ItemStack(reward.getIconMaterial());
            statusLine = "§c✘ Locked";
        }

        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            String name = claimed ? reward.getClaimedIconName() : reward.getIconName();
            meta.setDisplayName(name.replace("%time%", reward.getFormattedTime()));

            List<String> lore = new ArrayList<>();
            List<String> baseLore = claimed ? reward.getClaimedIconLore() : reward.getIconLore();
            for (String line : baseLore) {
                lore.add(line.replace("%time%", reward.getFormattedTime()));
            }
            lore.add("");
            lore.add(statusLine);

            if (!claimed && !unlocked) {
                long remaining = reward.getTimeMinutes() - data.getPlaytimeMinutes();
                lore.add("§7Remaining: §f" + formatMinutes(Math.max(0, remaining)));
            }
            if (!hasPermission) {
                lore.add("§cMissing permission");
            }

            meta.setLore(lore);
            item.setItemMeta(meta);
        }

        return item;
    }

    private String formatMinutes(long minutes) {
        long hours = minutes / 60;
        long mins = minutes % 60;
        if (hours > 0) {
            return hours + "h " + mins + "min";
        }
        return mins + "min";
    }
}
