package dev.bjunior.bcore.util;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;

/** Helper baca/tulis file YAML di folder data plugin. */
public final class Yml {

    private Yml() {
    }

    public static YamlConfiguration load(JavaPlugin plugin, String name) {
        File file = new File(plugin.getDataFolder(), name);
        if (!file.exists()) {
            return new YamlConfiguration();
        }
        return YamlConfiguration.loadConfiguration(file);
    }

    public static void save(JavaPlugin plugin, YamlConfiguration config, String name) {
        File folder = plugin.getDataFolder();
        if (!folder.exists() && !folder.mkdirs()) {
            plugin.getLogger().warning("Tidak bisa membuat folder data plugin.");
            return;
        }
        try {
            config.save(new File(folder, name));
        } catch (IOException exception) {
            plugin.getLogger().log(Level.WARNING, "Gagal menyimpan " + name + ": " + exception.getMessage());
        }
    }
}
