package com.chungpr0.bookhub.common.util;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class SecurePasswordGenerator {

    private static final String LOWERCASE = "abcdefghijklmnopqrstuvwxyz";
    private static final String UPPERCASE = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String DIGITS = "0123456789";
    private static final String SPECIAL = "!@#$%^&*()-_=+";
    private static final String ALL_CHARS = LOWERCASE + UPPERCASE + DIGITS + SPECIAL;

    private static final SecureRandom RANDOM = new SecureRandom();

    private SecurePasswordGenerator() {
        // Prevent instantiation
    }

    /**
     * Generates a 12-character cryptographically secure temporary password satisfying Rule BR-01:
     * - At least one lowercase letter
     * - At least one uppercase letter
     * - At least one digit
     * - At least one special character
     * - No whitespace
     */
    public static String generateTemporaryPassword() {
        List<Character> chars = new ArrayList<>(12);

        // Guarantee at least one character from each mandatory character class
        chars.add(LOWERCASE.charAt(RANDOM.nextInt(LOWERCASE.length())));
        chars.add(UPPERCASE.charAt(RANDOM.nextInt(UPPERCASE.length())));
        chars.add(DIGITS.charAt(RANDOM.nextInt(DIGITS.length())));
        chars.add(SPECIAL.charAt(RANDOM.nextInt(SPECIAL.length())));

        // Fill remaining 8 characters randomly from all allowed characters
        for (int i = 4; i < 12; i++) {
            chars.add(ALL_CHARS.charAt(RANDOM.nextInt(ALL_CHARS.length())));
        }

        // Shuffle the characters so the guaranteed positions are randomized
        Collections.shuffle(chars, RANDOM);

        StringBuilder sb = new StringBuilder(12);
        for (char c : chars) {
            sb.append(c);
        }
        return sb.toString();
    }
}

