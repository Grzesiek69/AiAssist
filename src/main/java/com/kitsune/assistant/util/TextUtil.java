package com.kitsune.assistant.util;

import java.util.Locale;

public final class TextUtil {
    private TextUtil() {}
    public static String norm(String s) {
        if (s == null) return "";
        return s.trim().toLowerCase(Locale.ROOT);
    }
}
