package dev.bjunior.bcore.util;

import dev.bjunior.bcore.BCorePlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;

import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * Dasar command BCore: cek permission, kirim pesan messages.yml, tab-complete sederhana.
 */
public abstract class BaseCommand implements TabExecutor {

    protected final BCorePlugin plugin;

    protected BaseCommand(BCorePlugin plugin) {
        this.plugin = plugin;
    }

    protected Msg msg() {
        return plugin.msg();
    }

    protected boolean requirePermission(CommandSender sender, String permission) {
        if (permission == null || permission.isBlank() || sender.hasPermission(permission)) {
            return true;
        }
        msg().send(sender, "general.no-permission", "&cKamu tidak punya izin untuk ini.");
        return false;
    }

    @Override
    public abstract boolean onCommand(CommandSender sender, Command command, String label, String[] args);

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return Collections.emptyList();
    }

    protected static boolean matches(String value, String prefix) {
        return value.toLowerCase(Locale.ROOT).startsWith(prefix.toLowerCase(Locale.ROOT));
    }
}
