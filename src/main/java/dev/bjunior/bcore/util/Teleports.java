package dev.bjunior.bcore.util;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * Teleport dengan countdown: batal kalau pemain bergerak, mati, atau keluar.
 */
public final class Teleports {

    private Teleports() {
    }

    public static void delayed(JavaPlugin plugin, Player player, Location target, int seconds,
                               double radius, String actionBar, Runnable onArrive, Runnable onCancel) {
        if (target == null) {
            onCancel.run();
            return;
        }
        if (seconds <= 0) {
            player.teleport(target);
            onArrive.run();
            return;
        }
        Location start = player.getLocation().clone();
        new BukkitRunnable() {
            int remaining = seconds;

            @Override
            public void run() {
                if (!player.isOnline()) {
                    cancel();
                    onCancel.run();
                    return;
                }
                if (player.isDead() || !player.getWorld().equals(start.getWorld())
                        || player.getLocation().distanceSquared(start) > radius * radius) {
                    cancel();
                    player.sendActionBar(Msg.of("&cTeleport dibatalkan."));
                    onCancel.run();
                    return;
                }
                if (remaining <= 0) {
                    cancel();
                    player.teleport(target);
                    onArrive.run();
                    return;
                }
                if (actionBar != null && !actionBar.isBlank()) {
                    player.sendActionBar(Msg.of(actionBar.replace("{time}", String.valueOf(remaining))));
                }
                remaining--;
            }
        }.runTaskTimer(plugin, 0L, 20L);
    }
}
