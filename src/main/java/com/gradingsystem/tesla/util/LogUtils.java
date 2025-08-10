package com.gradingsystem.tesla.util;

public final class LogUtils {
    private LogUtils() {}
    
    public static String truncate(String text, int maxLength) {
        if (text == null) return "";
        return text.length() > maxLength ? text.substring(0, maxLength) + "...[truncated]" : text;
    }
}

