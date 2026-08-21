package com.playtimerewards.commands;

import com.playtimerewards.PlaytimeRewards;
import com.playtimerewards.data.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Handles the /playtimerewards command (alias /pt) and its subcommands:
 * (no args)       - opens the GUI
 * reload          - reloads config.yml and messages.yml
 * time [player]   - shows playtime
 * add <p> <min>   - adds minutes (debug/admin)
 * reset <player>  - resets a player's data
 */
public class PlaytimeCommand implements CommandExecutor, TabCompleter {

    private final PlaytimeRewards plugin;

    public PlaytimeCommand(PlaytimeRewards plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (args.length == 0) {
            openGui(sender);
            return true;
        }

        String sub = args[0].toLowerCase();
        switch (sub) {
            case "reload" -> handleReload(sender);
            case "time" -> handleTime(sender, args);
            case "add" -> handleAdd(sender, args);
            case "reset" -> handleReset(sender, args);
            default -> openGui(sender);
        }
        return true;
    }

    private void openGui(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(plugin.getConfigManager().getMessage("errors.players-only", null));
            return;
        }
        if (!player.hasPermission("playtimerewards.use")) {
            player.sendMessage(plugin.getConfigManager().getMessage("errors.no-permission", player.getName()));
            return;
        }
        player.openInventory(plugin.getGuiManager().build(player));
    }

    private void handleReload(CommandSender sender) {
        if (!sender.hasPermission("playtimerewards.reload")) {
            sender.sendMessage(plugin.getConfigManager().getMessage("errors.no-permission", sender.getName()));
            return;
        }
        plugin.getConfigManager().reload();
        sender.sendMessage(plugin.getConfigManager().getMessage("commands.reload-success", sender.getName()));
    }

    private void handleTime(CommandSender sender, String[] args) {
        Player target;

        if (args.length >= 2) {
            if (!sender.hasPermission("playtimerewards.time.others")) {
                sender.sendMessage(plugin.getConfigManager().getMessage("errors.no-permission", sender.getName()));
                return;
            }
            target = Bukkit.getPlayer(args[1]);
            if (target == null) {
                sender.sendMessage(plugin.getConfigManager().getMessage("errors.player-not-found", sender.getName()));
                return;
            }
        } else {
            if (!(sender instanceof Player player)) {
                sender.sendMessage(plugin.getConfigManager().getMessage("errors.players-only", null));
                return;
            }
            target = player;
        }

        PlayerData data = plugin.getPlayerDataManager().getData(target.getUniqueId());
        long minutes = data.getPlaytimeMinutes();
        long hours = minutes / 60;
        long remMinutes = minutes % 60;

        String msg = plugin.getConfigManager().getMessage("commands.time-info", target.getName())
                .replace("%hours%", String.valueOf(hours))
                .replace("%minutes%", String.valueOf(remMinutes))
                .replace("%total_minutes%", String.valueOf(minutes));
        sender.sendMessage(msg);
    }

    private void handleAdd(CommandSender sender, String[] args) {
        if (!sender.hasPermission("playtimerewards.add")) {
            sender.sendMessage(plugin.getConfigManager().getMessage("errors.no-permission", sender.getName()));
            return;
        }
        if (args.length < 3) {
            sender.sendMessage(plugin.getConfigManager().getMessage("errors.usage-add", null));
            return;
        }

        OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
        UUID uuid = target.getUniqueId();

        long minutesToAdd;
        try {
            minutesToAdd = Long.parseLong(args[2]);
        } catch (NumberFormatException e) {
            sender.sendMessage(plugin.getConfigManager().getMessage("errors.invalid-number", null));
            return;
        }

        PlayerData data = plugin.getPlayerDataManager().getData(uuid);
        data.addMinutes(minutesToAdd);
        plugin.getPlayerDataManager().save(data);

        String name = target.getName() != null ? target.getName() : args[1];
        sender.sendMessage(plugin.getConfigManager().getMessage("commands.add-success", name)
                .replace("%amount%", String.valueOf(minutesToAdd)));
    }

    private void handleReset(CommandSender sender, String[] args) {
        if (!sender.hasPermission("playtimerewards.admin")) {
            sender.sendMessage(plugin.getConfigManager().getMessage("errors.no-permission", sender.getName()));
            return;
        }
        if (args.length < 2) {
            sender.sendMessage(plugin.getConfigManager().getMessage("errors.usage-reset", null));
            return;
        }

        OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
        plugin.getPlayerDataManager().reset(target.getUniqueId());

        String name = target.getName() != null ? target.getName() : args[1];
        sender.sendMessage(plugin.getConfigManager().getMessage("commands.reset-success", name));
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, String[] args) {
        List<String> result = new ArrayList<>();
        if (args.length == 1) {
            for (String sub : List.of("reload", "time", "add", "reset")) {
                if (sub.startsWith(args[0].toLowerCase())) {
                    result.add(sub);
                }
            }
        } else if (args.length == 2 && List.of("time", "add", "reset").contains(args[0].toLowerCase())) {
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (p.getName().toLowerCase().startsWith(args[1].toLowerCase())) {
                    result.add(p.getName());
                }
            }
        }
        return result;
    }
}
