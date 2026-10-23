package dev.bjunior.bcore.listener;

import dev.bjunior.bcore.economy.EcoService;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public final class PlayerListener implements Listener {

    private final EcoService economy;

    public PlayerListener(EcoService economy) {
        this.economy = economy;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onJoin(PlayerJoinEvent event) {
        var player = event.getPlayer();
        economy.remember(player.getUniqueId(), player.getName());
        if (economy.autoCreateEnabled()) {
            economy.createAccount(player.getUniqueId(), player.getName());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onQuit(PlayerQuitEvent event) {
        economy.remember(event.getPlayer().getUniqueId(), event.getPlayer().getName());
    }
}
