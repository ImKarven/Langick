package me.karven.langick;

import me.karven.langick.exception.InvalidLanguageException;
import me.karven.langick.exception.InvalidTranslationKeyException;
import me.karven.langick.language.TranslationCache;
import me.karven.langick.language.VersionedLanguage;
import me.karven.langick.utility.Precondition;
import me.karven.langick.utility.StringUtility;
import org.apache.commons.io.FileUtils;
import org.jspecify.annotations.NonNull;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class Langick {
    private static final AtomicInteger THREAD_INDEX = new AtomicInteger(0);

    private final TranslationCache translationCache;
    private final File cacheDirectory;
    private final ExecutorService executorService;
    private @NonNull String urlFormat = "https://assets.mcasset.cloud/${version}/assets/minecraft/lang/${language}.json";
    private final AtomicInteger connectionTimeoutMillis = new AtomicInteger(10000);
    private final AtomicInteger readTimeoutMillis = new AtomicInteger(10000);

    public void setConnectionTimeoutMillis(final int connectionTimeoutMillis) {
        this.connectionTimeoutMillis.set(connectionTimeoutMillis);
    }

    public void setReadTimeoutMillis(final int readTimeoutMillis) {
        this.readTimeoutMillis.set(readTimeoutMillis);
    }

    public Langick() {
        this(defaultCacheDirectory());
    }

    public void setUrlFormat(final @NonNull String urlFormat) {
        this.urlFormat = urlFormat;
    }

    public Langick(final @NonNull File cacheDirectory) {
        this(cacheDirectory, 4);
    }

    public Langick(final @NonNull File cacheDirectory, final int threadPoolSize) {
        Precondition.checkArgument(cacheDirectory.isDirectory(), "Cache directory must be a directory");
        this.cacheDirectory = cacheDirectory;
        this.translationCache = new TranslationCache(cacheDirectory);
        this.executorService = Executors.newFixedThreadPool(threadPoolSize, runnable -> new Thread(runnable, "LangickThread-" + THREAD_INDEX.getAndIncrement()));
    }

    /**
     * Download a language from the internet using the current thread and save it to a file
     * @param language the language to download
     */
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void downloadLanguage(final VersionedLanguage language) {
        final URL url = getDownloadURL(language);
        final File versionDirectory = new File(cacheDirectory, language.version());
        versionDirectory.mkdirs();
        final File languageFile = new File(versionDirectory, language.language() + ".json");
        if (languageFile.exists()) return;
        try {
            FileUtils.copyURLToFile(url, languageFile, connectionTimeoutMillis.get(), readTimeoutMillis.get());
        } catch (final IOException exception) {
            throw new RuntimeException(exception);
        }
    }

    /**
     * Schedule a task to download a language on Langick thread pool
     * @param language the language to download
     * @return a future which is completed after the download is complete
     */
    public @NonNull CompletableFuture<Void> downloadLanguageAsync(final VersionedLanguage language) {
        final CompletableFuture<Void> future = new CompletableFuture<>();
        executorService.execute(() -> {
            downloadLanguage(language);
            future.complete(null);
        });
        return future;
    }

    /**
     * Get string of a translation key with a specified language
     * @param language the language
     * @param translationKey the translation key
     * @return the string that correlates to the translation key
     * @throws InvalidLanguageException if the provided language is invalid or not downloaded
     * @throws InvalidTranslationKeyException if the translation key does not exist in the language
     */
    public @NonNull String resolveTranslation(final VersionedLanguage language, final @NonNull String translationKey) throws InvalidLanguageException, InvalidTranslationKeyException {
        final Map<String, String> translations = translationCache.getTranslations(language);
        if (translations == null)
            throw new InvalidLanguageException("Cannot load " + language.language() + " version " + language.version() + " (Is it downloaded?)");
        final String resolvedString = translations.get(translationKey);
        if (resolvedString == null)
            throw new InvalidTranslationKeyException(language.language() + " version " + language.version() + " does not have translation for key " + translationKey);
        return resolvedString;
    }

    /**
     * Get the url to download a language file based on the current url format
     * @param language the language
     * @return an URL to download that language file
     */
    private @NonNull URL getDownloadURL(final VersionedLanguage language) {
        final String stringUrl = StringUtility.format(urlFormat, Map.of("language", language.language(), "version", language.version()));
        try {
            return new URI(stringUrl).toURL();
        } catch (final URISyntaxException | MalformedURLException exception) {
            throw new RuntimeException(exception);
        }
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private static File defaultCacheDirectory() {
        final File cacheDirectory = new File("langick");
        cacheDirectory.mkdirs();
        return cacheDirectory;
    }
}
