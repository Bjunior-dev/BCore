package dev.bjunior.bcore.util;

import java.util.Locale;

/**
 * Utilitas format angka, waktu, dan parsing nominal (1k / 2.5m / 1b).
 */
public final class FormatUtil {

    private static final String[] SUFFIXES = {"", "K", "M", "B", "T", "Q"};

    private FormatUtil() {
    }

    /** 1234567 -> "1.23M" */
    public static String number(double value) {
        if (Double.isNaN(value) || Double.isInfinite(value)) {
            return "0";
        }
        boolean negative = value < 0.0D;
        double abs = Math.abs(value);
        int index = 0;
        while (abs >= 1000.0D && index < SUFFIXES.length - 1) {
            abs /= 1000.0D;
            index++;
        }
        String text;
        if (index == 0) {
            text = String.format(Locale.US, "%,.2f", abs);
        } else if (abs >= 100.0D) {
            text = String.format(Locale.US, "%.0f", abs) + SUFFIXES[index];
        } else if (abs >= 10.0D) {
            text = String.format(Locale.US, "%.1f", abs) + SUFFIXES[index];
        } else {
            text = String.format(Locale.US, "%.2f", abs) + SUFFIXES[index];
        }
        return (negative ? "-" : "") + text;
    }

    /** 3725 -> "1h 2m 5s" */
    public static String time(long seconds) {
        if (seconds <= 0L) {
            return "0s";
        }
        long days = seconds / 86_400L;
        long hours = (seconds % 86_400L) / 3_600L;
        long minutes = (seconds % 3_600L) / 60L;
        long secs = seconds % 60L;
        StringBuilder builder = new StringBuilder();
        if (days > 0L) {
            builder.append(days).append("d ");
        }
        if (hours > 0L) {
            builder.append(hours).append("h ");
        }
        if (minutes > 0L) {
            builder.append(minutes).append("m ");
        }
        if (secs > 0L && days == 0L) {
            builder.append(secs).append("s");
        }
        return builder.toString().trim();
    }

    /** "1.5k" -> 1500, input tidak valid -> fallback. */
    public static double parseAmount(String input, double fallback) {
        if (input == null || input.isBlank()) {
            return fallback;
        }
        String cleaned = input.trim().toLowerCase(Locale.US).replace(",", "").replace("_", "");
        double multiplier = 1.0D;
        char last = cleaned.charAt(cleaned.length() - 1);
        switch (last) {
            case 'k' -> {
                multiplier = 1_000D;
                cleaned = cleaned.substring(0, cleaned.length() - 1);
            }
            case 'm' -> {
                multiplier = 1_000_000D;
                cleaned = cleaned.substring(0, cleaned.length() - 1);
            }
            case 'b' -> {
                multiplier = 1_000_000_000D;
                cleaned = cleaned.substring(0, cleaned.length() - 1);
            }
            case 't' -> {
                multiplier = 1_000_000_000_000D;
                cleaned = cleaned.substring(0, cleaned.length() - 1);
            }
            default -> {
                // tanpa suffix
            }
        }
        if (cleaned.isBlank()) {
            return fallback;
        }
        try {
            double value = Double.parseDouble(cleaned) * multiplier;
            if (Double.isNaN(value) || Double.isInfinite(value) || value <= 0.0D) {
                return fallback;
            }
            return value;
        } catch (NumberFormatException exception) {
            return fallback;
        }
    }

    /** Nama home/argumen aman: huruf, angka, underscore, strip. */
    public static boolean isSafeName(String input) {
        return input != null && input.length() <= 16 && input.matches("[A-Za-z0-9_-]+");
    }
}
