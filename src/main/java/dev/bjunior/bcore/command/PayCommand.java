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

public final class PayCommand implements TabExecutor {

    private final JavaPlugin plugin;
    private final EcoService economy;

    public PayCommand(JavaPlugin plugin, EcoService economy) {
        this.plugin = plugin;
        this.economy = economy;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Hanya pemain yang bisa memakai /pay.");
            return true;
        }
        if (args.length < 2) {
            sender.sendMessage("Pemakaian: /pay <player> <jumlah>");
            return true;
        }

        Player target = Bukkit.getPlayerExact(args[0]);
        if (target == null) {
            sender.sendMessage("Pemain harus online: " + args[0]);
            return true;
        }
        if (target.getUniqueId().equals(player.getUniqueId())) {
            sender.sendMessage("Tidak bisa mengirim uang ke diri sendiri.");
            return true;
        }

        double amount;
        try {
            amount = Double.parseDouble(args[1].replace(",", ".").replace("_", ""));
        } catch (NumberFormatException exception) {
            sender.sendMessage("Jumlah tidak valid: " + args[1]);
            return true;
        }
        if (amount <= 0.0D) {
            sender.sendMessage("Jumlah harus lebih dari 0.");
            return true;
        }

        economy.remember(player.getUniqueId(), player.getName());
        economy.remember(target.getUniqueId(), target.getName());

        if (!economy.transfer(player.getUniqueId(), target.getUniqueId(), amount)) {
            sender.sendMessage("Saldo tidak cukup. Saldo kamu: "
                    + economy.format(economy.getBalance(player.getUniqueId())));
            return true;
        }

        sender.sendMessage("Kamu mengirim " + economy.format(amount) + " ke " + target.getName()
                + ". Sisa saldo: " + economy.format(economy.getBalance(player.getUniqueId())));
        target.sendMessage("Kamu menerima " + economy.format(amount) + " dari " + player.getName()
                + ". Saldo: " + economy.format(economy.getBalance(target.getUniqueId())));
        return true;
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
