package com.devlaunch.util;

/**
 * Small formatting helpers shared across service implementations.
 * <p>
 * Kept intentionally minimal — helpers are added here only when the
 * same formatting logic is needed in more than one service.
 * </p>
 *
 * @author DevLaunch
 */
public final class EnumLabels {

    private EnumLabels() {
        // Utility class — no instantiation
    }

    /**
     * Converts an enum constant to a readable title-case label.
     * <p>
     * Example: {@code IN_PROGRESS} → {@code In Progress}.
     * </p>
     *
     * @param value the enum constant to format
     * @return the human-readable label, or an empty string if {@code null}
     */
    public static String toLabel(final Enum<?> value) {
        if (value == null) {
            return "";
        }
        final String[] words = value.name().toLowerCase().split("_");
        final StringBuilder label = new StringBuilder();
        for (final String word : words) {
            if (!label.isEmpty()) {
                label.append(' ');
            }
            label.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1));
        }
        return label.toString();
    }

}
