package dev.bjunior.bcore.command;

import dev.bjunior.bcore.BCorePlugin;
import dev.bjunior.bcore.util.Msg;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Basis command: parsing angka, filter tab-complete, prefix. */
public abstract class Sub implements TabExecutor {

    protected final BCorePlugin plugin;

    protected Sub(BCorePlugin plugin) {
        this.plugin = plugin;
    }

    protected String prefix() {
        return plugin.msg().raw("prefix", "&8[&bBCore&8] &r");
    }

    protected void msg(CommandSender to, String text) {
        if (text != null && !text.isBlank()) {
            to.sendMessage(Msg.of(text));
        }
    }

    protected void prefixed(CommandSender to, String text) {
        msg(to, prefix() + text);
    }

    protected static Double number(String raw) {
        try {
            return Double.parseDouble(raw.replace(",", "").replace("_", ""));
        } catch (NumberFormatException exception) {
            return null;
        }
    }

    protected static Integer integer(String raw) {
        try {
            return Integer.parseInt(raw);
        } catch (NumberFormatException exception) {
            return null;
        }
    }

    protected static List<String> filter(List<String> options, String token) {
        String lower = token.toLowerCase(Locale.ROOT);
        List<String> result = new ArrayList<>();
        for (String option : options) {
            if (option.toLowerCase(Locale.ROOT).startsWith(lower)) {
                result.add(option);
            }
        }
        return result;
    }

    @Override
    public abstract boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                                      @NotNull String label, @NotNull String[] args);

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                      @NotNull String alias, @NotNull String[] args) {
        return List.of();
    }
}
