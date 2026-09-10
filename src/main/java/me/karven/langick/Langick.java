package me.karven.langick;

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

public class Langick {

    private @NonNull String urlFormat = "https://mcasset.cloud/${version}/assets/minecraft/lang/${language}.json";
    private int connectionTimeoutMillis = 10000;
    private int readTimeoutMillis = 10000;
    private final File cacheDirectory;

    public void setConnectionTimeoutMillis(final int connectionTimeoutMillis) {
        this.connectionTimeoutMillis = connectionTimeoutMillis;
    }

    public void setReadTimeoutMillis(final int readTimeoutMillis) {
        this.readTimeoutMillis = readTimeoutMillis;
    }

    public Langick(final @NonNull File cacheDirectory) {
        Precondition.checkArgument(cacheDirectory.isDirectory(), "Cache directory must be a directory");
        this.cacheDirectory = cacheDirectory;
    }

    public void downloadLanguage(final @NonNull String minecraftLanguage, final @NonNull String version) {
        final URL url = getDownloadURL(minecraftLanguage, version);
        final File languageFile = new File(cacheDirectory, minecraftLanguage + ".json");
        try {
            FileUtils.copyURLToFile(url, languageFile, connectionTimeoutMillis, readTimeoutMillis);
        } catch (final IOException exception) {
            throw new RuntimeException(exception);
        }
    }

    private @NonNull URL getDownloadURL(final @NonNull String minecraftLanguage, final @NonNull String version) {
        final String stringUrl = StringUtility.format(urlFormat, Map.of("language", minecraftLanguage, "version", version));
        try {
            return new URI(stringUrl).toURL();
        } catch (final URISyntaxException | MalformedURLException exception) {
            throw new RuntimeException(exception);
        }
    }
}
