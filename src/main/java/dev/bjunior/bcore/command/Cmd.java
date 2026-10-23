package dev.bjunior.bcore.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Command generik berbasis lambda supaya modul tidak perlu class terpisah
 * untuk tiap command.
 */
public final class Cmd implements TabExecutor {

    @FunctionalInterface
    public interface Handler {
        boolean run(CommandSender sender, String[] args);
    }

    @FunctionalInterface
    public interface Completer {
        List<String> complete(CommandSender sender, String[] args);
    }

    private final Handler handler;
    private Completer completer;

    private Cmd(Handler handler) {
        this.handler = handler;
    }

    public static Cmd of(Handler handler) {
        return new Cmd(handler);
    }

    public Cmd tab(Completer completer) {
        this.completer = completer;
        return this;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {
        return handler.run(sender, args);
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                      @NotNull String alias, @NotNull String[] args) {
        if (completer == null) {
            return List.of();
        }
        List<String> result = completer.complete(sender, args);
        return result == null ? List.of() : result;
    }
}
