package dev.bjunior.bcore.module.impl;

import dev.bjunior.bcore.BCorePlugin;
import dev.bjunior.bcore.command.Cmd;
import dev.bjunior.bcore.data.PlayerData;
import dev.bjunior.bcore.util.Tab;
import dev.bjunior.bcore.util.Teleport;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

/** /sethome, /home, /delhome, /homes */
public final class HomesModule extends ModuleBase {

    public HomesModule(BCorePlugin plugin) {
        super(plugin);
    }

    @Override
    public String id() {
        return "homes";
    }

    @Override
    public void enable() {
        plugin.registerCommand("sethome", Cmd.of((sender, args) -> {
            if (!(sender instanceof Player player)) {
                send(sender, "&cHanya untuk pemain.");
                return true;
            }
            PlayerData data = plugin.data().get(player);
            String name = args.length > 0 ? args[0].toLowerCase() : "home";
            if (!data.hasHome(name) && data.homes().size() >= max()) {
                plugin.msg().send(player, "homes.limit", "&cBatas home kamu &e{max}&c.", "max", max());
                return true;
            }
            data.setHome(name, player.getLocation());
            plugin.msg().send(player, "homes.set", "&aHome &e{home} &adisimpan.", "home", name);
            return true;
        }).tab((sender, args) -> List.of("home")));

        plugin.registerCommand("delhome", Cmd.of((sender, args) -> {
            if (!(sender instanceof Player player)) {
                send(sender, "&cHanya untuk pemain.");
                return true;
            }
            if (args.length == 0) {
                plugin.msg().send(player, "homes.usage", "&7Pakai: &e/homes");
                return true;
            }
            String name = args[0].toLowerCase();
            PlayerData data = plugin.data().get(player);
            if (!data.hasHome(name)) {
                plugin.msg().send(player, "homes.not-found", "&cHome &e{home} &ctidak ada.", "home", name);
                return true;
            }
            data.removeHome(name);
            plugin.msg().send(player, "homes.deleted", "&aHome &e{home} &adihapus.", "home", name);
            return true;
        }).tab((sender, args) -> {
            if (args.length == 1 && sender instanceof Player player) {
                return Tab.match(plugin.data().get(player).homeNames(), args[0]);
            }
            return List.of();
        }));

        plugin.registerCommand("home", Cmd.of((sender, args) -> {
            if (!(sender instanceof Player player)) {
                send(sender, "&cHanya untuk pemain.");
                return true;
            }
            if (args.length == 0) {
                list(player);
                return true;
            }
            String name = args[0].toLowerCase();
            PlayerData data = plugin.data().get(player);
            Location target = data.home(name);
            if (target == null) {
                plugin.msg().send(player, "homes.not-found", "&cHome &e{home} &ctidak ada.", "home", name);
                return true;
            }
            if (!allowedWorld(target)) {
                plugin.msg().send(player, "homes.world-blocked", "&cTidak bisa memakai home di dunia ini.");
                return true;
            }
            Teleport.to(plugin, player, target, name);
            return true;
        }).tab((sender, args) -> {
            if (args.length == 1 && sender instanceof Player player) {
                return Tab.match(plugin.data().get(player).homeNames(), args[0]);
            }
            return List.of();
        }));

        plugin.registerCommand("homes", Cmd.of((sender, args) -> {
            if (!(sender instanceof Player player)) {
                send(sender, "&cHanya untuk pemain.");
                return true;
            }
            list(player);
            return true;
        }));
    }

    private void list(Player player) {
        List<String> names = plugin.data().get(player).homeNames();
        plugin.msg().sendList(player, "homes.list-header", List.of("&8&m-----&r &bHome Kamu &8&m-----"));
        if (names.isEmpty()) {
            plugin.msg().send(player, "homes.list-empty", "&7Belum ada home.");
            return;
        }
        List<String> lines = new ArrayList<>();
        for (String name : names) {
            lines.add("&e- " + name);
        }
        plugin.msg().sendList(player, "homes.list-lines", lines);
    }

    private boolean allowedWorld(Location location) {
        List<String> enabled = plugin.getConfig().getStringList("homes.enabled-worlds");
        if (enabled.isEmpty()) {
            return true;
        }
        return enabled.contains(location.getWorld().getName());
    }

    private int max() {
        return Math.max(1, integer("homes.max", 5));
    }

    @Override
    public void disable() {
    }
}
