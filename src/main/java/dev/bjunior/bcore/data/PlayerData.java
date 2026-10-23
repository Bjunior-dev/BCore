package dev.bjunior.bcore.data;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Data satu pemain: homes, shard, statistik, toggle, bounty/cooldown.
 * Disimpan sebagai YAML per-UUID oleh {@link PlayerDataStore}.
 */
public final class PlayerData {

    private final UUID uuid;
    private String name;
    private final Map<String, String> homes = new ConcurrentHashMap<>();
    private final Map<String, Long> cooldowns = new ConcurrentHashMap<>();
    private final Map<String, Integer> counters = new ConcurrentHashMap<>();

    private long shards;
    private long kills;
    private long deaths;
    private long blocksBroken;
    private long blocksPlaced;
    private long playtimeSeconds;
    private long joins;
    private long lastSeen;

    private boolean fly;
    private boolean nightVision;
    private boolean phantoms;
    private boolean mobSpawn;
    private boolean scoreboard;
    private boolean tpaEnabled = true;
    private boolean payEnabled = true;
    private boolean chatMuted;
    private boolean afk;

    private String combatWorld;
    private double combatX;
    private double combatY;
    private double combatZ;
    private boolean combatLogged;

    private transient long combatUntil;
    private transient long lastMoveCheck;
    private transient long sessionStart = System.currentTimeMillis();

    public PlayerData(UUID uuid) {
        this.uuid = uuid;
    }

    public UUID uuid() {
        return uuid;
    }

    public String name() {
        return name == null ? uuid.toString().substring(0, 8) : name;
    }

    public void name(String name) {
        if (name != null && !name.isBlank()) {
            this.name = name;
        }
    }

    // ------------------------------------------------------------------ homes

    public Map<String, String> homes() {
        return homes;
    }

    public boolean hasHome(String home) {
        return homes.containsKey(home.toLowerCase());
    }

    public void setHome(String home, Location location) {
        homes.put(home.toLowerCase(), encode(location));
    }

    public void removeHome(String home) {
        homes.remove(home.toLowerCase());
    }

    public Location home(String home) {
        String raw = homes.get(home.toLowerCase());
        return raw == null ? null : decode(raw);
    }

    public List<String> homeNames() {
        return new ArrayList<>(homes.keySet());
    }

    public static String encode(Location location) {
        if (location == null) {
            return "";
        }
        return String.join(";",
                location.getWorld().getName(),
                trim(location.getX()), trim(location.getY()), trim(location.getZ()),
                trim(location.getYaw()), trim(location.getPitch()));
    }

    public static Location decode(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        String[] parts = raw.split(";");
        if (parts.length < 4) {
            return null;
        }
        World world = Bukkit.getWorld(parts[0]);
        if (world == null) {
            return null;
        }
        try {
            double x = Double.parseDouble(parts[1]);
            double y = Double.parseDouble(parts[2]);
            double z = Double.parseDouble(parts[3]);
            float yaw = parts.length > 4 ? Float.parseFloat(parts[4]) : 0.0F;
            float pitch = parts.length > 5 ? Float.parseFloat(parts[5]) : 0.0F;
            return new Location(world, x, y, z, yaw, pitch);
        } catch (NumberFormatException exception) {
            return null;
        }
    }

    private static String trim(double value) {
        return String.format(java.util.Locale.US, "%.3f", value);
    }

    // -------------------------------------------------------------- statistik

    public long shards() {
        return shards;
    }

    public void shards(long value) {
        this.shards = Math.max(0L, value);
    }

    public void addShards(long value) {
        shards(Math.addExact(shards, value));
    }

    public long kills() {
        return kills;
    }

    public long deaths() {
        return deaths;
    }

    public long blocksBroken() {
        return blocksBroken;
    }

    public long blocksPlaced() {
        return blocksPlaced;
    }

    public long joins() {
        return joins;
    }

    public long playtimeSeconds() {
        return playtimeSeconds + Math.max(0L, (System.currentTimeMillis() - sessionStart) / 1000L);
    }

    public void addKill() {
        kills++;
    }

    public void addDeath() {
        deaths++;
    }

    public void addBlockBroken() {
        blocksBroken++;
    }

    public void addBlockPlaced() {
        blocksPlaced++;
    }

    public void addJoin() {
        joins++;
    }

    public long lastSeen() {
        return lastSeen;
    }

    public void touch() {
        lastSeen = System.currentTimeMillis();
    }

    public void stopSession() {
        playtimeSeconds = playtimeSeconds();
        sessionStart = System.currentTimeMillis();
    }

    // ---------------------------------------------------------------- toggle

    public boolean fly() {
        return fly;
    }

    public void fly(boolean value) {
        this.fly = value;
    }

    public boolean nightVision() {
        return nightVision;
    }

    public void nightVision(boolean value) {
        this.nightVision = value;
    }

    public boolean phantoms() {
        return phantoms;
    }

    public void phantoms(boolean value) {
        this.phantoms = value;
    }

    public boolean mobSpawn() {
        return mobSpawn;
    }

    public void mobSpawn(boolean value) {
        this.mobSpawn = value;
    }

    public boolean scoreboard() {
        return scoreboard;
    }

    public void scoreboard(boolean value) {
        this.scoreboard = value;
    }

    public boolean tpaEnabled() {
        return tpaEnabled;
    }

    public void tpaEnabled(boolean value) {
        this.tpaEnabled = value;
    }

    public boolean payEnabled() {
        return payEnabled;
    }

    public void payEnabled(boolean value) {
        this.payEnabled = value;
    }

    public boolean chatMuted() {
        return chatMuted;
    }

    public void chatMuted(boolean value) {
        this.chatMuted = value;
    }

    public boolean afk() {
        return afk;
    }

    public void afk(boolean value) {
        this.afk = value;
    }

    // --------------------------------------------------------------- cooldown

    public boolean onCooldown(String key) {
        Long until = cooldowns.get(key);
        return until != null && until > System.currentTimeMillis();
    }

    public long cooldownLeft(String key) {
        Long until = cooldowns.get(key);
        if (until == null) {
            return 0L;
        }
        return Math.max(0L, (until - System.currentTimeMillis()) / 1000L);
    }

    public void cooldown(String key, long seconds) {
        if (seconds <= 0L) {
            cooldowns.remove(key);
            return;
        }
        cooldowns.put(key, System.currentTimeMillis() + seconds * 1000L);
    }

    public int counter(String key) {
        return counters.getOrDefault(key, 0);
    }

    public void counter(String key, int value) {
        counters.put(key, value);
    }

    // ----------------------------------------------------------------- combat

    public boolean inCombat() {
        return combatUntil > System.currentTimeMillis();
    }

    public long combatLeft() {
        return Math.max(0L, (combatUntil - System.currentTimeMillis()) / 1000L);
    }

    public void tagCombat(long seconds) {
        combatUntil = System.currentTimeMillis() + seconds * 1000L;
    }

    public void clearCombat() {
        combatUntil = 0L;
        combatLogged = false;
    }

    public boolean combatLogged() {
        return combatLogged;
    }

    public void combatLogged(boolean value) {
        this.combatLogged = value;
    }

    public void rememberCombatLocation(Player player) {
        Location location = player.getLocation();
        this.combatWorld = location.getWorld().getName();
        this.combatX = location.getX();
        this.combatY = location.getY();
        this.combatZ = location.getZ();
    }

    public Location combatLocation() {
        if (combatWorld == null) {
            return null;
        }
        World world = Bukkit.getWorld(combatWorld);
        return world == null ? null : new Location(world, combatX, combatY, combatZ);
    }

    // -------------------------------------------------------------- serialize

    public YamlConfiguration serialize() {
        YamlConfiguration yaml = new YamlConfiguration();
        yaml.set("name", name);
        yaml.set("shards", shards);
        ConfigurationSection stats = yaml.createSection("stats");
        stats.set("kills", kills);
        stats.set("deaths", deaths);
        stats.set("blocks-broken", blocksBroken);
        stats.set("blocks-placed", blocksPlaced);
        stats.set("playtime-seconds", playtimeSeconds());
        stats.set("joins", joins);
        stats.set("last-seen", System.currentTimeMillis());
        ConfigurationSection toggles = yaml.createSection("toggles");
        toggles.set("fly", fly);
        toggles.set("night-vision", nightVision);
        toggles.set("phantoms", phantoms);
        toggles.set("mob-spawn", mobSpawn);
        toggles.set("scoreboard", scoreboard);
        toggles.set("tpa", tpaEnabled);
        toggles.set("pay", payEnabled);
        ConfigurationSection homeSection = yaml.createSection("homes");
        homes.forEach(homeSection::set);
        ConfigurationSection cooldownSection = yaml.createSection("cooldowns");
        long now = System.currentTimeMillis();
        cooldowns.forEach((key, until) -> {
            if (until != null && until > now) {
                cooldownSection.set(key, until);
            }
        });
        return yaml;
    }

    public static PlayerData deserialize(UUID uuid, ConfigurationSection section) {
        PlayerData data = new PlayerData(uuid);
        if (section == null) {
            return data;
        }
        data.name(section.getString("name"));
        data.shards = section.getLong("shards", 0L);
        data.kills = section.getLong("stats.kills", 0L);
        data.deaths = section.getLong("stats.deaths", 0L);
        data.blocksBroken = section.getLong("stats.blocks-broken", 0L);
        data.blocksPlaced = section.getLong("stats.blocks-placed", 0L);
        data.playtimeSeconds = section.getLong("stats.playtime-seconds", 0L);
        data.joins = section.getLong("stats.joins", 0L);
        data.lastSeen = section.getLong("stats.last-seen", 0L);
        data.fly = section.getBoolean("toggles.fly", false);
        data.nightVision = section.getBoolean("toggles.night-vision", false);
        data.phantoms = section.getBoolean("toggles.phantoms", true);
        data.mobSpawn = section.getBoolean("toggles.mob-spawn", true);
        data.scoreboard = section.getBoolean("toggles.scoreboard", true);
        data.tpaEnabled = section.getBoolean("toggles.tpa", true);
        data.payEnabled = section.getBoolean("toggles.pay", true);
        ConfigurationSection homeSection = section.getConfigurationSection("homes");
        if (homeSection != null) {
            for (String key : homeSection.getKeys(false)) {
                String value = homeSection.getString(key);
                if (value != null) {
                    data.homes.put(key.toLowerCase(), value);
                }
            }
        }
        ConfigurationSection cooldownSection = section.getConfigurationSection("cooldowns");
        if (cooldownSection != null) {
            for (String key : cooldownSection.getKeys(false)) {
                data.cooldowns.put(key, cooldownSection.getLong(key));
            }
        }
        return data;
    }

    public Map<String, Long> cooldownSnapshot() {
        return new LinkedHashMap<>(cooldowns);
    }
}
