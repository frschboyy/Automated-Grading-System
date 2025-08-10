package com.gradingsystem.tesla.util;

public class PathUtils {

    // Sanitizes a string to be safe for use in file paths.
    // Replaces all non-alphanumeric characters (except - and _) with underscores.
    public static String sanitizePathPart(String input) {
        if (input == null || input.isBlank()) {
            return "unnamed";
        }
        return input.replaceAll("[^a-zA-Z0-9_-]", "_");
    }
}
