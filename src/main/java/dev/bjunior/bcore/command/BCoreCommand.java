package dev.bjunior.bcore.command;

import dev.bjunior.bcore.economy.EcoService;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class BCoreCommand implements TabExecutor {

    private final JavaPlugin plugin;
    private final EcoService economy;

    public BCoreCommand(JavaPlugin plugin, EcoService economy) {
        this.plugin = plugin;
        this.economy = economy;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {
        String sub = args.length == 0 ? "version" : args[0].toLowerCase(Locale.ROOT);

        if (sub.equals("reload")) {
            if (!sender.hasPermission("bcore.eco.admin")) {
                sender.sendMessage("Kamu tidak punya izin untuk reload.");
                return true;
            }
            plugin.reloadConfig();
            economy.save();
            economy.load();
            sender.sendMessage("[BCore] Config & data dimuat ulang.");
            return true;
        }

        sender.sendMessage("[BCore] versi " + plugin.getPluginMeta().getVersion()
                + " | server " + plugin.getServer().getVersion());
        sender.sendMessage("Akun tersimpan: " + economy.size()
                + " | API: " + plugin.getPluginMeta().getAPIVersion());
        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                      @NotNull String alias, @NotNull String[] args) {
        List<String> result = new ArrayList<>();
        if (args.length == 1) {
            String prefix = args[0].toLowerCase(Locale.ROOT);
            for (String option : List.of("version", "reload")) {
                if (option.startsWith(prefix)) {
                    result.add(option);
                }
            }
        }
        return result;
    }
}
