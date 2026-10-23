package dev.bjunior.bcore.command;

import dev.bjunior.bcore.economy.EcoService;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class BaltopCommand implements TabExecutor {

    private static final int MAX_LINES = 10;

    private final JavaPlugin plugin;
    private final EcoService economy;

    public BaltopCommand(JavaPlugin plugin, EcoService economy) {
        this.plugin = plugin;
        this.economy = economy;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {
        List<Map.Entry<UUID, Double>> top = economy.top(MAX_LINES);
        if (top.isEmpty()) {
            sender.sendMessage("Belum ada data saldo.");
            return true;
        }

        sender.sendMessage("=== Top " + top.size() + " saldo ===");
        int rank = 1;
        for (Map.Entry<UUID, Double> entry : top) {
            String name = economy.nameOf(entry.getKey())
                    .orElseGet(() -> {
                        String bukkitName = Bukkit.getOfflinePlayer(entry.getKey()).getName();
                        return bukkitName == null ? entry.getKey().toString().substring(0, 8) : bukkitName;
                    });
            sender.sendMessage(rank + ". " + name + " - " + economy.format(entry.getValue()));
            rank++;
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                      @NotNull String alias, @NotNull String[] args) {
        return Collections.emptyList();
    }
}
