package com.chungpr0.bookhub.common.util;

import java.util.regex.Pattern;

public final class PhoneUtils {

    private static final Pattern VN_PHONE_PATTERN = Pattern.compile("^(0|\\+84)(3|5|7|8|9)[0-9]{8}$");

    private PhoneUtils() {
    }

    public static boolean isValid(String phone) {
        if (phone == null || phone.isBlank()) {
            return false;
        }
        return VN_PHONE_PATTERN.matcher(phone.trim()).matches();
    }

    public static String normalize(String phone) {
        if (phone == null) {
            return null;
        }
        String trimmed = phone.trim();
        if (trimmed.startsWith("+84")) {
            return "0" + trimmed.substring(3);
        }
        return trimmed;
    }
}

