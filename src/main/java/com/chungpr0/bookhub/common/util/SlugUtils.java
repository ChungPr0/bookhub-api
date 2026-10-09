package com.chungpr0.bookhub.common.util;

import java.text.Normalizer;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Utility class to convert strings into URL-friendly slugs for Vietnamese and international text.
 */
public final class SlugUtils {

    private static final Pattern NONLATIN = Pattern.compile("[^\\w-]");
    private static final Pattern WHITESPACE = Pattern.compile("[\\s+]");
    private static final Pattern COMBINING_MARKS = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
    private static final Pattern MULTI_HYPHEN = Pattern.compile("-+");

    private SlugUtils() {
        // Prevent instantiation per zero-warning utility class convention
    }

    /**
     * Converts a Vietnamese or English text into an SEO slug.
     * E.g. "Nhà Giả Kim (Tái Bản 2026)" -> "nha-gia-kim-tai-ban-2026"
     *
     * @param input Raw text
     * @return Normalized slug
     */
    public static String toSlug(String input) {
        if (input == null || input.isBlank()) {
            return "";
        }

        String normalized = input.trim();
        // Replace special Vietnamese characters
        normalized = normalized.replace("đ", "d").replace("Đ", "d");

        // Decompose accented characters and strip combining diacritical marks
        normalized = Normalizer.normalize(normalized, Normalizer.Form.NFD);
        normalized = COMBINING_MARKS.matcher(normalized).replaceAll("");

        // Replace whitespace with hyphen
        String noWhitespace = WHITESPACE.matcher(normalized).replaceAll("-");

        // Remove non-latin and non-alphanumeric characters except hyphen
        String normalizedSlug = NONLATIN.matcher(noWhitespace).replaceAll("");

        // Consolidate multiple hyphens and trim
        normalizedSlug = MULTI_HYPHEN.matcher(normalizedSlug).replaceAll("-");
        normalizedSlug = normalizedSlug.toLowerCase(Locale.ENGLISH);

        if (normalizedSlug.startsWith("-")) {
            normalizedSlug = normalizedSlug.substring(1);
        }
        if (normalizedSlug.endsWith("-")) {
            normalizedSlug = normalizedSlug.substring(0, normalizedSlug.length() - 1);
        }

        return normalizedSlug;
    }
}

