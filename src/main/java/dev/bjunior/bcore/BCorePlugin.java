package dev.bjunior.bcore;

import dev.bjunior.bcore.command.BCoreCommand;
import dev.bjunior.bcore.command.BalanceCommand;
import dev.bjunior.bcore.command.BaltopCommand;
import dev.bjunior.bcore.command.EcoAdminCommand;
import dev.bjunior.bcore.command.PayCommand;
import dev.bjunior.bcore.data.PlayerDataStore;
import dev.bjunior.bcore.economy.EcoService;
import dev.bjunior.bcore.listener.PlayerListener;
import dev.bjunior.bcore.module.CoreModule;
import dev.bjunior.bcore.module.ModuleManager;
import dev.bjunior.bcore.module.impl.AfkModule;
import dev.bjunior.bcore.module.impl.AuctionModule;
import dev.bjunior.bcore.module.impl.BountyModule;
import dev.bjunior.bcore.module.impl.ChatModule;
import dev.bjunior.bcore.module.impl.ClearLagModule;
import dev.bjunior.bcore.module.impl.CombatModule;
import dev.bjunior.bcore.module.impl.HomesModule;
import dev.bjunior.bcore.module.impl.LeaderboardModule;
import dev.bjunior.bcore.module.impl.RtpModule;
import dev.bjunior.bcore.module.impl.SettingsModule;
import dev.bjunior.bcore.module.impl.ShardsModule;
import dev.bjunior.bcore.module.impl.SpawnModule;
import dev.bjunior.bcore.module.impl.StatsModule;
import dev.bjunior.bcore.module.impl.TpaModule;
import dev.bjunior.bcore.module.impl.WarpModule;
import dev.bjunior.bcore.util.Msg;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabExecutor;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;

/**
 * BCore - core ekonomi + semua modul server (homes, tpa, spawn, warps, rtp, afk,
 * clearlag, combat, bounty, shards, stats, leaderboard, settings, chat, auction).
 *
 * Ditulis ulang bersih (bukan hasil dekompilasi) agar source-nya jelas dan bisa
 * dikembangkan. Semua modul bisa dinyalakan/dimatikan dari config.yml.
 */
public final class BCorePlugin extends JavaPlugin {

    private EcoService economy;
    private PlayerDataStore dataStore;
    private ModuleManager modules;
    private Msg messages;
    private BukkitTask autosaveTask;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        messages = new Msg(this, "messages.yml");

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

        dataStore = new PlayerDataStore(this);
        modules = new ModuleManager(this);

        bind("balance", new BalanceCommand(this, economy));
        bind("pay", new PayCommand(this, economy));
        bind("baltop", new BaltopCommand(this, economy));
        bind("eco", new EcoAdminCommand(this, economy));
        bind("bcore", new BCoreCommand(this, economy));

        registerModules();

        getServer().getPluginManager().registerEvents(new PlayerListener(economy, dataStore), this);
        modules.enableConfigured();
        startAutosave();

        getLogger().info("BCore " + getPluginMeta().getVersion()
                + " aktif (API " + getPluginMeta().getAPIVersion() + ", Java 25)."
                + " Akun tersimpan: " + economy.size());
    }

    @Override
    public void onDisable() {
        if (autosaveTask != null) {
            autosaveTask.cancel();
            autosaveTask = null;
        }
        if (modules != null) {
            modules.disableAll();
        }
        if (dataStore != null) {
            dataStore.saveAllSync();
            dataStore.clear();
        }
        if (economy != null) {
            economy.save();
            economy.clear();
        }
        getLogger().info("BCore nonaktif, data disimpan.");
    }

    private void registerModules() {
        addModule(new HomesModule(this));
        addModule(new TpaModule(this));
        addModule(new SpawnModule(this));
        addModule(new WarpModule(this));
        addModule(new RtpModule(this));
        addModule(new AfkModule(this));
        addModule(new ClearLagModule(this));
        addModule(new CombatModule(this));
        addModule(new BountyModule(this));
        addModule(new ShardsModule(this));
        addModule(new StatsModule(this));
        addModule(new LeaderboardModule(this));
        addModule(new SettingsModule(this));
        addModule(new ChatModule(this));
        addModule(new AuctionModule(this));
    }

    private void addModule(CoreModule module) {
        modules.register(module);
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

    /** Didaftarkan oleh modul. Command harus ada di plugin.yml. */
    public void registerCommand(String name, TabExecutor executor) {
        bind(name, executor);
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
                dataStore.saveAllAsync();
            } catch (Throwable throwable) {
                getLogger().warning("Autosave gagal: " + throwable.getMessage());
            }
        }, ticks, ticks);
    }

    public void reloadEverything() {
        reloadConfig();
        messages.reload();
        economy.load();
        modules.reload();
    }

    public EcoService economy() {
        return economy;
    }

    public PlayerDataStore data() {
        return dataStore;
    }

    public ModuleManager modules() {
        return modules;
    }

    public Msg msg() {
        return messages;
    }
}
