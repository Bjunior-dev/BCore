package dev.bjunior.bcore.gui;

import dev.bjunior.bcore.util.Msg;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Menu GUI sederhana: daftar slot + aksi klik.
 */
public final class SimpleMenu implements InventoryHolder {

    private final Inventory inventory;
    private final Map<Integer, Consumer<Player>> actions = new HashMap<>();
    private Consumer<Player> fallback;

    public SimpleMenu(int rows, String title) {
        int size = Math.max(1, Math.min(6, rows)) * 9;
        this.inventory = Bukkit.createInventory(this, size, Msg.of(title));
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }

    public SimpleMenu set(int slot, ItemStack item, Consumer<Player> action) {
        if (slot < 0 || slot >= inventory.getSize()) {
            return this;
        }
        inventory.setItem(slot, item);
        if (action != null) {
            actions.put(slot, action);
        }
        return this;
    }

    public SimpleMenu set(int slot, ItemStack item) {
        return set(slot, item, null);
    }

    public SimpleMenu onClick(Consumer<Player> action) {
        this.fallback = action;
        return this;
    }

    public SimpleMenu fill(ItemStack filler) {
        for (int slot = 0; slot < inventory.getSize(); slot++) {
            if (inventory.getItem(slot) == null) {
                inventory.setItem(slot, filler);
            }
        }
        return this;
    }

    public SimpleMenu fillRow(int row, ItemStack filler) {
        int start = row * 9;
        for (int slot = start; slot < start + 9; slot++) {
            if (slot < inventory.getSize()) {
                inventory.setItem(slot, filler);
            }
        }
        return this;
    }

    public void open(Player player) {
        player.openInventory(inventory);
    }

    void handle(Player player, int slot) {
        Consumer<Player> action = actions.get(slot);
        if (action != null) {
            action.accept(player);
        } else if (fallback != null) {
            fallback.accept(player);
        }
    }
}
