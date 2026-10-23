package dev.bjunior.bcore;

import dev.bjunior.bcore.command.BCoreCommand;
import dev.bjunior.bcore.command.BalanceCommand;
import dev.bjunior.bcore.command.BaltopCommand;
import dev.bjunior.bcore.command.EcoAdminCommand;
import dev.bjunior.bcore.command.PayCommand;
import dev.bjunior.bcore.economy.EcoService;
import dev.bjunior.bcore.listener.PlayerListener;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabExecutor;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;

/**
 * BCore - core ekonomi sederhana + pondasi event engine.
 * Ditulis ulang (bukan hasil dekompilasi) agar source-nya jelas dan bisa dikembangkan.
 */
public final class BCorePlugin extends JavaPlugin {

    private EcoService economy;
    private BukkitTask autosaveTask;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        File balancesFile = new File(getDataFolder(), "balances.yml");
        economy = new EcoService(
                this,
                balancesFile,
                getConfig().getBoolean("economy.auto-create", true),
                getConfig().getDouble("economy.starting-balance", 0.0D),
                getConfig().getDouble("economy.max-balance", 1_000_000_000_000D),
                getConfig().getString("format.currency", "$"),
                getConfig().getString("format.pattern", "#,##0.##")
        );
        economy.load();

        bind("balance", new BalanceCommand(this, economy));
        bind("pay", new PayCommand(this, economy));
        bind("baltop", new BaltopCommand(this, economy));
        bind("eco", new EcoAdminCommand(this, economy));
        bind("bcore", new BCoreCommand(this, economy));

        getServer().getPluginManager().registerEvents(new PlayerListener(economy), this);
        startAutosave();

        getLogger().info("BCore aktif (api 26.x / Java 25). Akun tersimpan: " + economy.size());
    }

    @Override
    public void onDisable() {
        if (autosaveTask != null) {
            autosaveTask.cancel();
            autosaveTask = null;
        }
        if (economy != null) {
            economy.save();
            economy.clear();
        }
        getLogger().info("BCore nonaktif, saldo disimpan.");
    }

    private void bind(String name, TabExecutor executor) {
        PluginCommand command = getCommand(name);
        if (command == null) {
            getLogger().warning("Command /" + name + " tidak terdaftar di plugin.yml");
            return;
        }
        command.setExecutor(executor);
        command.setTabCompleter(executor);
    }

    private void startAutosave() {
        long seconds = getConfig().getLong("storage.save-interval-seconds", 300L);
        if (seconds <= 0L) {
            getLogger().info("Autosave dimatikan (save-interval-seconds <= 0).");
            return;
        }
        long ticks = seconds * 20L;
        autosaveTask = getServer().getScheduler().runTaskTimerAsynchronously(this, () -> {
            try {
                economy.save();
            } catch (Throwable throwable) {
                getLogger().warning("Autosave gagal: " + throwable.getMessage());
            }
        }, ticks, ticks);
    }

    /** Dipakai modul lain (mis. EcoEvents) untuk mengambil layanan ekonomi. */
    public EcoService economy() {
        return economy;
    }
}
