package com.pser.auction.util;

import java.util.Date;
import java.util.Objects;
import org.springframework.core.env.Environment;

public class Util {
    public static long getNowMilliseconds() {
        Date now = new Date();
        return now.getTime();
    }

    public static Date afterHours(int hours) {
        long nowMilliseconds = getNowMilliseconds();
        int offsetMilliseconds = 1000 * 60 * 60 * hours;
        return new Date(nowMilliseconds + offsetMilliseconds);
    }

    public static int getIntProperty(Environment env, String key) {
        String value = Objects.requireNonNull(env.getProperty(key));
        return Integer.parseInt(value);
    }

    public static Long getLongProperty(Environment env, String key) {
        String value = Objects.requireNonNull(env.getProperty(key));
        return Long.parseLong(value);
    }

    public static boolean getBooleanProperty(Environment env, String key) {
        String value = Objects.requireNonNull(env.getProperty(key));
        return Boolean.parseBoolean(value);
    }

    public static boolean isNullOrBlank(String value) {
        if (value == null) {
            return true;
        }
        return value.isBlank();
    }
}
