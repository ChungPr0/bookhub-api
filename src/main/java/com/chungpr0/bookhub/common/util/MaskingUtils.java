package com.chungpr0.bookhub.common.util;

import java.util.Set;

public final class MaskingUtils {

    private static final Set<String> SENSITIVE_FIELDS = Set.of(
            "password",
            "newpassword",
            "currentpassword",
            "confirmpassword",
            "otpcode",
            "refreshtoken",
            "resettoken"
    );

    private MaskingUtils() {
    }

    public static String maskPhone(String phone) {
        if (phone == null || phone.length() < 7) {
            return phone;
        }
        String normalized = PhoneUtils.normalize(phone);
        if (normalized.length() >= 10) {
            return normalized.substring(0, 3) + "****" + normalized.substring(normalized.length() - 3);
        }
        return normalized;
    }

    public static Object maskIfSensitive(String fieldName, Object value) {
        if (fieldName == null || value == null) {
            return value;
        }
        String lower = fieldName.toLowerCase().replaceAll("[^a-z]", "");
        for (String sensitive : SENSITIVE_FIELDS) {
            if (lower.contains(sensitive)) {
                return "******";
            }
        }
        return value;
    }
}

