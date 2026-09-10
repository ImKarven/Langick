package me.karven.langick.language;

import org.jspecify.annotations.NullMarked;

@NullMarked
public record VersionedLanguage(String version, String language) {
}
