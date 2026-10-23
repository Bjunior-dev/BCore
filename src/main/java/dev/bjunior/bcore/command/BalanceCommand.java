package dev.bjunior.bcore.command;

import dev.bjunior.bcore.economy.EcoService;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class BalanceCommand implements TabExecutor {

    private final JavaPlugin plugin;
    private final EcoService economy;

    public BalanceCommand(JavaPlugin plugin, EcoService economy) {
        this.plugin = plugin;
        this.economy = economy;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {
        if (args.length == 0) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage("Gunakan /balance <player> dari console.");
                return true;
            }
            economy.remember(player.getUniqueId(), player.getName());
            sender.sendMessage("Saldo kamu: " + economy.format(economy.getBalance(player.getUniqueId())));
            return true;
        }

        if (!sender.hasPermission("bcore.balance.others")) {
            sender.sendMessage("Kamu tidak punya izin melihat saldo pemain lain.");
            return true;
        }

        OfflinePlayer target = resolve(args[0]);
        if (target == null) {
            sender.sendMessage("Pemain tidak ditemukan: " + args[0]);
            return true;
        }
        if (!economy.hasAccount(target.getUniqueId()) && !economy.autoCreateEnabled()) {
            sender.sendMessage("Pemain itu belum punya akun.");
            return true;
        }
        String name = target.getName() == null ? args[0] : target.getName();
        sender.sendMessage("Saldo " + name + ": " + economy.format(economy.getBalance(target.getUniqueId())));
        return true;
    }

    @SuppressWarnings("deprecation")
    private OfflinePlayer resolve(String input) {
        Player online = Bukkit.getPlayerExact(input);
        if (online != null) {
            return online;
        }
        OfflinePlayer offline = Bukkit.getOfflinePlayer(input);
        return offline.hasPlayedBefore() || offline.isOnline() ? offline : null;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                      @NotNull String alias, @NotNull String[] args) {
        List<String> result = new ArrayList<>();
        if (args.length == 1) {
            String prefix = args[0].toLowerCase(Locale.ROOT);
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (player.getName().toLowerCase(Locale.ROOT).startsWith(prefix)) {
                    result.add(player.getName());
                }
            }
        }
        return result;
    }
}
