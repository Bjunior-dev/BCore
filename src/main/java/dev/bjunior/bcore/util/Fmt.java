package dev.bjunior.bcore.util;

import java.util.Locale;

/** Format angka ringkas (1K/1M/1B/1T) dan durasi. */
public final class Fmt {

    private Fmt() {
    }

    public static String compact(double value) {
        double abs = Math.abs(value);
        if (abs >= 1_000_000_000_000D) return trim(value / 1_000_000_000_000D) + "T";
        if (abs >= 1_000_000_000D) return trim(value / 1_000_000_000D) + "B";
        if (abs >= 1_000_000D) return trim(value / 1_000_000D) + "M";
        if (abs >= 1_000D) return trim(value / 1_000D) + "K";
        return trim(value);
    }

    public static String trim(double value) {
        if (value == Math.rint(value)) return Long.toString((long) value);
        return String.format(Locale.US, "%.2f", value);
    }

    public static String duration(long seconds) {
        long s = Math.max(0L, seconds);
        long days = s / 86400L;
        long hours = (s % 86400L) / 3600L;
        long minutes = (s % 3600L) / 60L;
        long secs = s % 60L;
        StringBuilder sb = new StringBuilder();
        if (days > 0) sb.append(days).append("d ");
        if (hours > 0) sb.append(hours).append("h ");
        if (minutes > 0) sb.append(minutes).append("m ");
        if (sb.isEmpty() || secs > 0) sb.append(secs).append("s");
        return sb.toString().trim();
    }
}
