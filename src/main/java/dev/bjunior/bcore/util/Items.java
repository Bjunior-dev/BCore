package dev.bjunior.bcore.util;

import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

/**
 * Pembuat ItemStack cepat: nama + lore berwarna.
 */
public final class Items {

    private Items() {
    }

    public static ItemStack make(Material material, String name, String... lore) {
        ItemStack stack = new ItemStack(material == null ? Material.STONE : material);
        ItemMeta meta = stack.getItemMeta();
        if (meta == null) {
            return stack;
        }
        if (name != null) {
            meta.displayName(Msg.of(name));
        }
        if (lore != null && lore.length > 0) {
            List<Component> lines = new ArrayList<>();
            for (String line : lore) {
                lines.add(Msg.of(line == null ? "" : line));
            }
            meta.lore(lines);
        }
        stack.setItemMeta(meta);
        return stack;
    }

    public static ItemStack filler(Material material) {
        return make(material, "&r", "");
    }

    public static ItemStack button(Material material, String name) {
        return make(material, name);
    }
}
