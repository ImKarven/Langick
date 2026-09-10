package me.karven.langick.language;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.io.*;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TranslationCache {
    private static final Gson GSON = new Gson();
    private static final Type TRANSLATION_TYPE = new TypeToken<Map<String, String>>() {}.getType();
    private final File cacheDirectory;
    private final ConcurrentHashMap<VersionedLanguage, Map<String, String>> translationCache = new ConcurrentHashMap<>();

    public TranslationCache(final @NonNull File cacheDirectory) {
        this.cacheDirectory = cacheDirectory;
    }

    public @Nullable Map<String, String> getTranslations(final VersionedLanguage language)  {
        return translationCache.computeIfAbsent(language, this::loadTranslation);
    }

    private @Nullable Map<@NonNull String, @NonNull String> loadTranslation(final @NonNull VersionedLanguage language) {
        final File versionDirectory = new File(cacheDirectory, language.version());
        final File languageFile = new File(versionDirectory, language.language() + ".json");
        if (!languageFile.exists()) return null;
        try (
                final Reader fileReader = new FileReader(languageFile);
                final JsonReader jsonReader = new JsonReader(fileReader)
                ) {
            final Map<String, String> parsedJson = GSON.fromJson(jsonReader, TRANSLATION_TYPE);
            if (parsedJson == null) return null;
            return Map.copyOf(parsedJson);
        } catch (final IOException exception) {
            throw new RuntimeException(exception);
        }
    }
}
