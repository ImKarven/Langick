package me.karven.langick.utility;

import org.jspecify.annotations.NonNull;

public class Precondition {
    public static void checkArgument(final boolean condition, final @NonNull String message) {
        if (condition) return;
        throw new IllegalArgumentException(message);
    }
}
