package net.lotushq.languages;

import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * LanguageManager is the access point for an plugin wanting to utilize Languages. Simply create a new
 * instance (preferably keeping it a singleton) and register your {@link MessageProvider} interfaces via
 * {@link #registerProvider(JavaPlugin, Class)} or {@link #registerProvider(File, Class)}. From there, locale-specific
 * instances may be retrieved using {@link #getMessageProvider(Class, Locale)}.
 */
public final class LanguageManager {

    private final Map<Class<? extends MessageProvider>, ConfigurationCache> storages = new HashMap<>();

    public void registerProvider(File directory, Class<? extends MessageProvider> providerClass) {
        Validate.isTrue(providerClass.isInterface(), "Only interfaces may be registered. If you have an implementation, you're doing it wrong.");
        storages.put(providerClass, new ConfigurationCache(directory, providerClass));
    }

    /**
     * Registers a message provider for the given plugin. Note that this will copy the yaml configuration files
     * from the plugin jar's resources and save them in the plugin data folder.
     * @param plugin the plugin instance
     * @param providerClass the interface to register.
     * @throws IllegalArgumentException if the provided class is NOT an interface
     */
    public void registerProvider(JavaPlugin plugin, Class<? extends MessageProvider> providerClass) {
        Validate.isTrue(providerClass.isInterface(), "Only interfaces may be registered. If you have an implementation, you're doing it wrong.");
        storages.put(providerClass, new ConfigurationCache(plugin, providerClass));
    }

    /**
     * Retrieves a locale-specific implementation of the requested MessageProvider.
     * @param providerClass the interface of the requested provider
     * @param locale the locale being requested.
     * @param <T> the type of the provider
     * @return a locale-specific implementation of the provider, or the english implementation if unavailable.
     */
    public <T extends MessageProvider> T getMessageProvider(Class<T> providerClass, Locale locale) {
        return providerClass.cast(storages.get(providerClass).getProxy(locale));
    }

    /**
     * Retrieves an implementation of the requested MessageProvider based on the provided Player's language settings.
     * @param providerClass the interface of thr requested provider
     * @param player the player to retrieve the locale from
     * @param <T> the type of the provider
     * @return the locale-specific implementation of the provider, or the english provider if unavailable.
     */
    public <T extends MessageProvider> T getMessageProvider(Class<T> providerClass, Player player) {
        Locale locale = toLocale(player.getLocale());
        return getMessageProvider(providerClass, locale);
    }

    /**
     * Gets the {@link ConfigurationCache} for the requested MessageProvider.
     * @param provider the provider interface
     * @return the configuration cache
     */
    public ConfigurationCache getConfigCache(Class<? extends MessageProvider> provider) {
        return storages.get(provider);
    }

    /**
     * Converts the provided locale/language string into a {@link Locale} instance.
     * Mainly useful when you need a Locale instance from the result of {@link Player#getLocale()}
     * @param localeString the locale string
     * @return the Locale instance
     */
    public static Locale toLocale(String localeString) {
        Locale locale = Locale.ENGLISH;

        try {
            String[] parts = localeString.split("_");
            locale = new Locale(parts[0]);
        } catch (IllegalArgumentException e) {
            Bukkit.getLogger().warning("Unable to translate Locale: " + localeString);
        }

        return locale;
    }

}
