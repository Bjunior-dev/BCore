package dev.bjunior.bcore.combat;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/** Status combat in-memory (tag PvP). */
public final class CombatService {

    private final Map<UUID, Long> tagged = new ConcurrentHashMap<>();

    public void tag(UUID uuid, long seconds) {
        if (uuid == null || seconds <= 0L) {
            return;
        }
        tagged.put(uuid, System.currentTimeMillis() + seconds * 1000L);
    }

    public boolean inCombat(UUID uuid) {
        Long until = tagged.get(uuid);
        if (until == null) {
            return false;
        }
        if (until <= System.currentTimeMillis()) {
            tagged.remove(uuid);
            return false;
        }
        return true;
    }

    public long secondsLeft(UUID uuid) {
        Long until = tagged.get(uuid);
        if (until == null) {
            return 0L;
        }
        return Math.max(0L, (until - System.currentTimeMillis()) / 1000L);
    }

    public void clear(UUID uuid) {
        tagged.remove(uuid);
    }

    public Map<UUID, Long> snapshot() {
        return Map.copyOf(tagged);
    }
}
