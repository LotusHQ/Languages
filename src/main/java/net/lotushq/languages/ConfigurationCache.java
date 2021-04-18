package net.lotushq.languages;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import net.lotushq.languages.annotation.ConfigurationSource;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.lang.reflect.Proxy;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

/**
 * ConfigurationCache is responsible for caching {@link MessageRetrievalHandler}s, which hold {@link YamlConfiguration}
 * instances, for every locale that has been requested OR accessed within the last 10 minutes.
 */
public final class ConfigurationCache {

    private final LoadingCache<Locale, MessageRetrievalHandler> cache;
    private final ConfigurationSource configSource;
    private final File directory;

    /**
     * Constructor used when the default language directory is used, which is under "languages" in the plugin's data
     * folder. Using this constructor will also copy the yaml configurations from the jar's resources to the data folder.
     *
     * @param plugin        the java plugin instance
     * @param providerClass the provider interface
     */
    public ConfigurationCache(JavaPlugin plugin, Class<? extends MessageProvider> providerClass) {
        this(new File(plugin.getDataFolder(), "languages"), providerClass);
        saveDefaultFiles(plugin);
    }

    /**
     * Constructor used when a custom language directory is needed. YAML configurations will NOT be copied from
     * the jar's resources to this directory.
     *
     * @param langDirectory the directory to retrieve the configurations from
     * @param providerClass the provider interface
     */
    public ConfigurationCache(File langDirectory, Class<? extends MessageProvider> providerClass) {
        this.configSource = providerClass.getAnnotation(ConfigurationSource.class);
        this.directory = new File(langDirectory, configSource.folderName());

        if (!directory.exists() && !directory.mkdirs()) {
            throw new IllegalStateException("Unable to create language file storage in " + directory.getPath());
        }

        this.cache = CacheBuilder.newBuilder()
                .expireAfterAccess(10, TimeUnit.MINUTES)
                .build(new CacheLoader<>() {
                    @Override
                    public MessageRetrievalHandler load(Locale key) {
                        File file = new File(directory, key.getLanguage() + ".yml");
                        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
                        MessageRetrievalHandler handler = new MessageRetrievalHandler(config);

                        MessageProvider proxy = (MessageProvider) Proxy.newProxyInstance(providerClass.getClassLoader(),
                                new Class[]{providerClass}, handler);

                        handler.setProxy(proxy);
                        return handler;
                    }
                });
    }

    /**
     * Retrieves the dynamic proxy implementation of {@link MessageProvider} for the requested locale.
     *
     * @param locale the locale being requested
     * @return the provider implementation attached to the requested locale.
     */
    MessageProvider getProxy(Locale locale) {
        try {
            return this.cache.get(locale).getProxy();
        } catch (ExecutionException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Copies the "languages" resources from the jar to the plugin's data folder, preserving the file structure.
     *
     * @param plugin the java plugin used to find the data folder.
     */
    private void saveDefaultFiles(JavaPlugin plugin) {
        final String relativePath = "languages" + File.separator + configSource.folderName() + File.separator;

        Stream.of(configSource.languages())
                .map(language -> relativePath + language + ".yml")
                .forEach(path -> plugin.saveResource(path, true));
    }

}
