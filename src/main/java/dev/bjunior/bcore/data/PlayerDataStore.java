package dev.bjunior.bcore.data;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

/**
 * Penyimpanan data pemain berbasis YAML (folder players/&lt;uuid&gt;.yml).
 * Semua akses dari thread utama; penulisan dilakukan di async thread.
 */
public final class PlayerDataStore {

    private final JavaPlugin plugin;
    private final File folder;
    private final Map<UUID, PlayerData> cache = new ConcurrentHashMap<>();

    public PlayerDataStore(JavaPlugin plugin) {
        this.plugin = plugin;
        this.folder = new File(plugin.getDataFolder(), "players");
        if (!folder.exists() && !folder.mkdirs()) {
            plugin.getLogger().warning("Tidak bisa membuat folder players.");
        }
    }

    public PlayerData get(UUID uuid) {
        return cache.computeIfAbsent(uuid, this::load);
    }

    public PlayerData get(Player player) {
        PlayerData data = get(player.getUniqueId());
        data.name(player.getName());
        return data;
    }

    public boolean isLoaded(UUID uuid) {
        return cache.containsKey(uuid);
    }

    public Collection<PlayerData> loaded() {
        return new ArrayList<>(cache.values());
    }

    private File fileOf(UUID uuid) {
        return new File(folder, uuid + ".yml");
    }

    private PlayerData load(UUID uuid) {
        File file = fileOf(uuid);
        if (!file.exists()) {
            return new PlayerData(uuid);
        }
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
        return PlayerData.deserialize(uuid, yaml);
    }

    public void save(UUID uuid) {
        PlayerData data = cache.get(uuid);
        if (data == null) {
            return;
        }
        YamlConfiguration yaml = data.serialize();
        File file = fileOf(uuid);
        try {
            yaml.save(file);
        } catch (IOException exception) {
            plugin.getLogger().log(Level.WARNING, "Gagal menyimpan data " + uuid + ": " + exception.getMessage(), exception);
        }
    }

    public void saveAllSync() {
        cache.keySet().forEach(this::save);
    }

    public void saveAllAsync() {
        Map<UUID, PlayerData> snapshot = Map.copyOf(cache);
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            for (Map.Entry<UUID, PlayerData> entry : snapshot.entrySet()) {
                try {
                    entry.getValue().serialize().save(fileOf(entry.getKey()));
                } catch (IOException exception) {
                    plugin.getLogger().warning("Autosave gagal untuk " + entry.getKey() + ": " + exception.getMessage());
                }
            }
        });
    }

    public void unload(UUID uuid) {
        PlayerData data = cache.remove(uuid);
        if (data != null) {
            data.stopSession();
            YamlConfiguration yaml = data.serialize();
            try {
                yaml.save(fileOf(uuid));
            } catch (IOException exception) {
                plugin.getLogger().warning("Gagal menyimpan data saat unload: " + exception.getMessage());
            }
        }
    }

    public void clear() {
        cache.clear();
    }

    /** Daftar semua player yang punya file data (untuk leaderboard offline). */
    public List<UUID> storedIds() {
        List<UUID> ids = new ArrayList<>();
        File[] files = folder.listFiles((dir, name) -> name.endsWith(".yml"));
        if (files == null) {
            return ids;
        }
        for (File file : files) {
            String name = file.getName().substring(0, file.getName().length() - 4);
            try {
                ids.add(UUID.fromString(name));
            } catch (IllegalArgumentException ignored) {
                // nama file bukan UUID, lewati
            }
        }
        return ids;
    }

    /** Baca nama pemain offline tanpa memuat seluruh data ke cache. */
    public String storedName(UUID uuid) {
        File file = fileOf(uuid);
        if (!file.exists()) {
            return null;
        }
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
        return yaml.getString("name");
    }

    public double storedShards(UUID uuid) {
        File file = fileOf(uuid);
        if (!file.exists()) {
            return 0.0D;
        }
        ConfigurationSection section = YamlConfiguration.loadConfiguration(file);
        return section.getLong("shards", 0L);
    }
}
