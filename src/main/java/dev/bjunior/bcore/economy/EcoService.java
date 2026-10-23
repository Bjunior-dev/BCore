package dev.bjunior.bcore.economy;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Penyimpanan saldo sederhana berbasis file (balances.yml).
 * - Saldo tidak pernah negatif dan dibatasi max-balance.
 * - Thread-safe (aman dipanggil dari async autosave).
 */
public final class EcoService {

    private final JavaPlugin plugin;
    private final File file;
    private final boolean autoCreate;
    private final double startingBalance;
    private final double maxBalance;
    private final String currency;
    private final DecimalFormat formatter;

    private final Map<UUID, Double> balances = new ConcurrentHashMap<>();
    private final Map<UUID, String> lastKnownName = new ConcurrentHashMap<>();

    public EcoService(JavaPlugin plugin,
                      File file,
                      boolean autoCreate,
                      double startingBalance,
                      double maxBalance,
                      String currency,
                      String pattern) {
        this.plugin = plugin;
        this.file = file;
        this.autoCreate = autoCreate;
        this.startingBalance = Math.max(0.0D, startingBalance);
        this.maxBalance = Math.max(1.0D, maxBalance);
        this.currency = currency == null ? "$" : currency;
        this.formatter = new DecimalFormat(
                pattern == null || pattern.isBlank() ? "#,##0.##" : pattern,
                DecimalFormatSymbols.getInstance(Locale.US));
    }

    public boolean autoCreateEnabled() {
        return autoCreate;
    }

    public void load() {
        balances.clear();
        lastKnownName.clear();
        if (!file.exists()) {
            return;
        }
        YamlConfiguration data = YamlConfiguration.loadConfiguration(file);
        if (data.getConfigurationSection("balances") == null) {
            return;
        }
        for (String raw : data.getConfigurationSection("balances").getKeys(false)) {
            try {
                UUID uuid = UUID.fromString(raw);
                double value = data.getDouble("balances." + raw + ".amount", 0.0D);
                String name = data.getString("balances." + raw + ".name", "");
                balances.put(uuid, clamp(value));
                if (name != null && !name.isBlank()) {
                    lastKnownName.put(uuid, name);
                }
            } catch (IllegalArgumentException ignored) {
                plugin.getLogger().warning("UUID tidak valid di balances.yml: " + raw);
            }
        }
    }

    public synchronized void save() {
        YamlConfiguration data = new YamlConfiguration();
        data.options().setHeader(List.of(
                "BCore - data saldo",
                "Jangan diedit saat server hidup."));
        for (Map.Entry<UUID, Double> entry : balances.entrySet()) {
            String path = "balances." + entry.getKey();
            data.set(path + ".amount", entry.getValue());
            data.set(path + ".name", lastKnownName.getOrDefault(entry.getKey(), ""));
        }
        try {
            if (!plugin.getDataFolder().exists() && !plugin.getDataFolder().mkdirs()) {
                plugin.getLogger().warning("Tidak bisa membuat folder plugin.");
            }
            data.save(file);
        } catch (IOException exception) {
            plugin.getLogger().warning("Gagal menyimpan balances.yml: " + exception.getMessage());
        }
    }

    public void clear() {
        balances.clear();
        lastKnownName.clear();
    }

    public int size() {
        return balances.size();
    }

    public boolean hasAccount(UUID uuid) {
        return balances.containsKey(uuid);
    }

    public boolean createAccount(UUID uuid, String name) {
        remember(uuid, name);
        if (balances.containsKey(uuid)) {
            return false;
        }
        balances.put(uuid, clamp(startingBalance));
        return true;
    }

    public double getBalance(UUID uuid) {
        Double value = balances.get(uuid);
        if (value == null) {
            if (autoCreate) {
                createAccount(uuid, null);
                return startingBalance;
            }
            return 0.0D;
        }
        return value;
    }

    /** Set saldo (dipakai admin). */
    public void set(UUID uuid, double amount) {
        balances.put(uuid, clamp(amount));
    }

    /** Tambah saldo, return saldo akhir. */
    public double deposit(UUID uuid, double amount) {
        if (amount <= 0.0D) {
            return getBalance(uuid);
        }
        return balances.merge(uuid, clamp(amount), (oldValue, add) -> clamp(oldValue + add));
    }

    /** Kurangi saldo bila cukup, return true kalau berhasil. */
    public synchronized boolean withdraw(UUID uuid, double amount) {
        if (amount <= 0.0D) {
            return false;
        }
        double current = getBalance(uuid);
        if (current < amount) {
            return false;
        }
        balances.put(uuid, clamp(current - amount));
        return true;
    }

    /** Transfer; gagal bila saldo kurang atau nominal tidak valid. */
    public synchronized boolean transfer(UUID from, UUID to, double amount) {
        if (from.equals(to) || amount <= 0.0D) {
            return false;
        }
        if (!withdraw(from, amount)) {
            return false;
        }
        deposit(to, amount);
        return true;
    }

    public void remember(UUID uuid, String name) {
        if (uuid != null && name != null && !name.isBlank()) {
            lastKnownName.put(uuid, name);
        }
    }

    public Optional<String> nameOf(UUID uuid) {
        return Optional.ofNullable(lastKnownName.get(uuid));
    }

    public String format(double amount) {
        synchronized (formatter) {
            return currency + formatter.format(amount);
        }
    }

    /** Top N saldo tertinggi. */
    public List<Map.Entry<UUID, Double>> top(int limit) {
        List<Map.Entry<UUID, Double>> sorted = new ArrayList<>(balances.entrySet());
        sorted.sort((a, b) -> Double.compare(b.getValue(), a.getValue()));
        if (sorted.size() > limit) {
            return new ArrayList<>(sorted.subList(0, limit));
        }
        return sorted;
    }

    public Map<UUID, Double> snapshot() {
        return Collections.unmodifiableMap(new LinkedHashMap<>(balances));
    }

    private double clamp(double value) {
        if (Double.isNaN(value) || Double.isInfinite(value)) {
            return 0.0D;
        }
        if (value < 0.0D) {
            return 0.0D;
        }
        return Math.min(value, maxBalance);
    }
}
