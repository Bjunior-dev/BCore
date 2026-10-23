package dev.bjunior.bcore.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Pembaca messages.yml: semua pesan plugin terpusat di satu file,
 * mendukung kode warna '&' dan placeholder {nama}.
 */
public final class Msg {

    public static final LegacyComponentSerializer LEGACY = LegacyComponentSerializer.legacyAmpersand();

    private final JavaPlugin plugin;
    private final File file;
    private YamlConfiguration config;

    public Msg(JavaPlugin plugin, String fileName) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), fileName);
        reload();
    }

    public void reload() {
        if (!plugin.getDataFolder().exists() && !plugin.getDataFolder().mkdirs()) {
            plugin.getLogger().warning("Tidak bisa membuat folder data plugin.");
        }
        if (!file.exists() && plugin.getResource(file.getName()) != null) {
            plugin.saveResource(file.getName(), false);
        }
        config = YamlConfiguration.loadConfiguration(file);
        InputStream defaults = plugin.getResource(file.getName());
        if (defaults != null) {
            config.setDefaults(YamlConfiguration.loadConfiguration(
                    new InputStreamReader(defaults, StandardCharsets.UTF_8)));
        }
    }

    /** Teks mentah (tanpa prefix). */
    public String raw(String path, String fallback) {
        String value = config.getString(path);
        return value == null ? fallback : value;
    }

    public boolean flag(String path, boolean fallback) {
        return config.getBoolean(path, fallback);
    }

    public String format(String path, String fallback, Object... placeholders) {
        String text = raw(path, fallback);
        for (int i = 0; i + 1 < placeholders.length; i += 2) {
            text = text.replace("{" + placeholders[i] + "}", String.valueOf(placeholders[i + 1]));
        }
        return text;
    }

    public Component component(String path, String fallback, Object... placeholders) {
        return LEGACY.deserialize(format(path, fallback, placeholders));
    }

    public void send(CommandSender sender, String path, String fallback, Object... placeholders) {
        String text = format(path, fallback, placeholders);
        if (text.isBlank()) {
            return;
        }
        if (text.startsWith("prefix:")) {
            text = raw("prefix", "") + text.substring("prefix:".length()).trim();
        }
        sender.sendMessage(LEGACY.deserialize(text));
    }

    public void sendList(CommandSender sender, String path, List<String> fallback, Object... placeholders) {
        List<String> lines = config.getStringList(path);
        if (lines.isEmpty()) {
            lines = fallback;
        }
        for (String line : lines) {
            for (int i = 0; i + 1 < placeholders.length; i += 2) {
                line = line.replace("{" + placeholders[i] + "}", String.valueOf(placeholders[i + 1]));
            }
            sender.sendMessage(LEGACY.deserialize(line));
        }
    }

    public void broadcast(String path, String fallback, Object... placeholders) {
        String text = format(path, fallback, placeholders);
        if (text.isBlank()) {
            return;
        }
        plugin.getServer().broadcast(LEGACY.deserialize(text));
    }

    public static Component of(String legacyText) {
        return LEGACY.deserialize(legacyText == null ? "" : legacyText);
    }
}
