package dev.bjunior.bcore.module;

import org.bukkit.plugin.java.JavaPlugin;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;

/**
 * Menyalakan/mematikan modul berdasarkan config.yml (modules.<id>).
 */
public final class ModuleManager {

    private final JavaPlugin plugin;
    private final Map<String, CoreModule> modules = new LinkedHashMap<>();
    private final Map<String, Boolean> active = new LinkedHashMap<>();

    public ModuleManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void register(CoreModule module) {
        modules.put(module.id().toLowerCase(Locale.ROOT), module);
    }

    public void enableConfigured() {
        for (Map.Entry<String, CoreModule> entry : modules.entrySet()) {
            String id = entry.getKey();
            CoreModule module = entry.getValue();
            boolean wanted = plugin.getConfig().getBoolean("modules." + id, true);
            if (!wanted) {
                active.put(id, false);
                continue;
            }
            try {
                module.enable();
                active.put(id, true);
            } catch (Throwable throwable) {
                active.put(id, false);
                plugin.getLogger().log(Level.WARNING,
                        "Modul '" + id + "' gagal diaktifkan: " + throwable.getMessage(), throwable);
            }
        }
    }

    public void disableAll() {
        for (CoreModule module : modules.values()) {
            try {
                module.disable();
            } catch (Throwable throwable) {
                plugin.getLogger().log(Level.FINE,
                        "Gagal menonaktifkan modul " + module.id() + ": " + throwable.getMessage(), throwable);
            }
        }
        active.replaceAll((id, value) -> false);
    }

    public void reload() {
        disableAll();
        plugin.reloadConfig();
        enableConfigured();
    }

    public boolean isActive(String id) {
        return Boolean.TRUE.equals(active.get(id.toLowerCase(Locale.ROOT)));
    }

    public Map<String, Boolean> status() {
        return Map.copyOf(active);
    }

    public CoreModule get(String id) {
        return modules.get(id.toLowerCase(Locale.ROOT));
    }
}
