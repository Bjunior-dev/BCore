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
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public final class EcoAdminCommand implements TabExecutor {

    private static final List<String> ACTIONS = Arrays.asList("give", "take", "set", "reset", "reload");

    private final JavaPlugin plugin;
    private final EcoService economy;

    public EcoAdminCommand(JavaPlugin plugin, EcoService economy) {
        this.plugin = plugin;
        this.economy = economy;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("bcore.eco.admin")) {
            sender.sendMessage("Kamu tidak punya izin untuk ini.");
            return true;
        }
        if (args.length == 0) {
            sender.sendMessage("Pemakaian: /eco <give|take|set|reset|reload> [player] [jumlah]");
            return true;
        }

        String action = args[0].toLowerCase(Locale.ROOT);
        if (action.equals("reload")) {
            plugin.reloadConfig();
            economy.save();
            economy.load();
            sender.sendMessage("[BCore] Config & saldo dimuat ulang. Akun: " + economy.size());
            return true;
        }

        if (!ACTIONS.contains(action)) {
            sender.sendMessage("Aksi tidak dikenal: " + action);
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage("Sebutkan nama pemain: /eco " + action + " <player> [jumlah]");
            return true;
        }

        OfflinePlayer target = resolve(args[1]);
        if (target == null) {
            sender.sendMessage("Pemain tidak ditemukan: " + args[1]);
            return true;
        }

        String displayName = target.getName() == null ? args[1] : target.getName();

        if (action.equals("reset")) {
            economy.set(target.getUniqueId(), 0.0D);
            economy.save();
            sender.sendMessage("[BCore] Saldo " + displayName + " direset ke 0.");
            return true;
        }

        if (args.length < 3) {
            sender.sendMessage("Sebutkan jumlah: /eco " + action + " " + displayName + " <jumlah>");
            return true;
        }

        double amount;
        try {
            amount = Double.parseDouble(args[2].replace(",", ".").replace("_", ""));
        } catch (NumberFormatException exception) {
            sender.sendMessage("Jumlah tidak valid: " + args[2]);
            return true;
        }
        if (amount < 0.0D) {
            sender.sendMessage("Jumlah tidak boleh negatif.");
            return true;
        }

        switch (action) {
            case "give" -> economy.deposit(target.getUniqueId(), amount);
            case "take" -> economy.withdraw(target.getUniqueId(), amount);
            case "set" -> economy.set(target.getUniqueId(), amount);
            default -> {
                sender.sendMessage("Aksi tidak dikenal: " + action);
                return true;
            }
        }

        double now = economy.getBalance(target.getUniqueId());
        economy.save();
        sender.sendMessage("[BCore] " + action + " " + amount + " -> " + displayName
                + ". Saldo sekarang: " + economy.format(now));

        Player online = target.getPlayer();
        if (online != null) {
            online.sendMessage("[BCore] Saldo kamu sekarang: " + economy.format(now));
        }
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
        String prefix = args[args.length - 1].toLowerCase(Locale.ROOT);
        if (args.length == 1) {
            for (String action : ACTIONS) {
                if (action.startsWith(prefix)) {
                    result.add(action);
                }
            }
        } else if (args.length == 2) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (player.getName().toLowerCase(Locale.ROOT).startsWith(prefix)) {
                    result.add(player.getName());
                }
            }
        }
        return result;
    }
}
