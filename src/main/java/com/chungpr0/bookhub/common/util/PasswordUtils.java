package com.chungpr0.bookhub.common.util;

import java.util.regex.Pattern;

public final class PasswordUtils {

    // 8-64 characters, at least 1 uppercase, at least 1 lowercase, at least 1 digit, no whitespace
    private static final Pattern BR01_PASSWORD_PATTERN =
            Pattern.compile("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)\\S{8,64}$");

    private PasswordUtils() {
    }

    public static boolean isValid(String password) {
        if (password == null) {
            return false;
        }
        return BR01_PASSWORD_PATTERN.matcher(password).matches();
    }
}

