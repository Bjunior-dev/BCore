package dev.bjunior.bcore.util;

import dev.bjunior.bcore.BCorePlugin;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/** Teleport dengan countdown; batal bila pemain bergerak (config teleportation.*). */
public final class Teleport {

    private static final Map<UUID, BukkitTask> TASKS = new ConcurrentHashMap<>();

    private Teleport() {
    }

    public static void cancel(UUID uuid) {
        BukkitTask task = TASKS.remove(uuid);
        if (task != null) {
            task.cancel();
        }
    }

    public static void to(BCorePlugin plugin, Player player, Location target, String label) {
        int seconds = Math.max(0, plugin.getConfig().getInt("teleportation.countdown-seconds", 5));
        boolean cancelOnMove = plugin.getConfig().getBoolean("teleportation.cancel-on-move", true);
        double radius = plugin.getConfig().getDouble("teleportation.radius-movement", 0.5D);

        cancel(player.getUniqueId());

        if (seconds <= 0) {
            player.teleportAsync(target);
            plugin.msg().send(player, "teleport.done", "&aTeleport ke &e{target}&a.", "target", label);
            return;
        }

        Location start = player.getLocation().clone();
        int[] left = {seconds};
        BukkitTask task = plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
            if (!player.isOnline()) {
                cancel(player.getUniqueId());
                return;
            }
            if (cancelOnMove) {
                Location now = player.getLocation();
                if (now.getWorld() != start.getWorld()
                        || now.distanceSquared(start) > radius * radius) {
                    cancel(player.getUniqueId());
                    plugin.msg().send(player, "teleport.moved", "&cTeleport dibatalkan karena kamu bergerak.");
                    return;
                }
            }
            if (left[0] <= 0) {
                cancel(player.getUniqueId());
                player.teleportAsync(target);
                plugin.msg().send(player, "teleport.done", "&aTeleport ke &e{target}&a.", "target", label);
                return;
            }
            plugin.msg().send(player, "teleport.countdown",
                    "&7Teleport ke &e{target} &7dalam &e{time}s&7.", "target", label, "time", left[0]);
            player.sendActionBar(Msg.of("&e" + left[0] + "s &7» &f" + label));
            left[0]--;
        }, 0L, 20L);

        TASKS.put(player.getUniqueId(), task);
    }

    public static void cancelAll() {
        TASKS.values().forEach(BukkitTask::cancel);
        TASKS.clear();
    }
}
