package net.lotushq.languages;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.apache.commons.lang.LocaleUtils;
import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.reflections.Reflections;
import org.reflections.scanners.ResourcesScanner;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

public final class FileStorage<T extends LanguageFile> {

    private final Cache<Locale, T> cache = CacheBuilder.newBuilder()
            .expireAfterAccess(10, TimeUnit.MINUTES)
            .build();

    private final File directory;
    private final T defaultLangFile;

    public FileStorage(JavaPlugin plugin, File baseDirectory, T defaultLangFile) {

        this.directory = baseDirectory;

        if (!directory.exists() && !directory.mkdirs()) {
            throw new IllegalStateException("Unable to create language file storage in " + baseDirectory.getName());
        }

        saveDefaultFiles(plugin);
        this.defaultLangFile = loadFile(Locale.ENGLISH, defaultLangFile);

    }

    private void saveDefaultFiles(JavaPlugin plugin) {
        Reflections reflections = new Reflections(null, new ResourcesScanner());
        Set<String> relativePaths = reflections.getResources(name -> name.endsWith(".yml"));
        relativePaths.forEach(path -> plugin.saveResource(path, true));
    }

    public T loadFile(Locale locale, T languageFile) {
        if (languageFile == null) return null;

        File file = new File(directory, locale.getLanguage() + ".yml");

        try {
            languageFile.load(file);
        } catch (IOException | InvalidConfigurationException e) {
            Bukkit.getLogger().warning("Unable to load language file: " +
                    languageFile.getClass().getSimpleName() + ", " + e.getLocalizedMessage());
            return null;
        }

        cache.put(locale, languageFile);
        return languageFile;
    }

    @SuppressWarnings("unchecked")
    public T get(Locale locale) {
        return Optional.ofNullable(cache.getIfPresent(locale))
                .or(() -> Optional.ofNullable(loadFile(locale, (T) LanguageFile.empty(defaultLangFile.getClass()))))
                .orElse(defaultLangFile);
    }

    public T get(Player player) {

        String localeString = player.getLocale();
        Locale locale = Locale.ENGLISH;

        try {
            String[] parts = localeString.split("_");
            locale = new Locale(parts[0], parts[1]);
        } catch (IllegalArgumentException e) {
            Bukkit.getLogger().warning("Unable to translate Locale: " + localeString);
        }

        return get(locale);

    }

}
