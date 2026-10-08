package com.chungpr0.bookhub.common.util;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public final class DateTimeUtils {

    public static final ZoneId VIETNAM_ZONE_ID = ZoneId.of("Asia/Ho_Chi_Minh");
    public static final DateTimeFormatter ISO_OFFSET_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX");

    private DateTimeUtils() {
    }

    public static OffsetDateTime nowVietnam() {
        return OffsetDateTime.now(VIETNAM_ZONE_ID);
    }

    public static String format(OffsetDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        return dateTime.atZoneSameInstant(VIETNAM_ZONE_ID).format(ISO_OFFSET_FORMATTER);
    }
}

