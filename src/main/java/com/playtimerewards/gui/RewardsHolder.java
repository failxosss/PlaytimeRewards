package com.playtimerewards.gui;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

/**
 * Empty "marker" holder - lets GUIListener safely recognize that a click
 * happened in an inventory belonging to this plugin, and not some other GUI.
 */
public class RewardsHolder implements InventoryHolder {

    private Inventory inventory;

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }

    public void setInventory(Inventory inventory) {
        this.inventory = inventory;
    }
}
