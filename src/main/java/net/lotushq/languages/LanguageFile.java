package net.lotushq.languages;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;

import java.lang.reflect.Constructor;
import java.util.Optional;

public abstract class LanguageFile extends YamlConfiguration implements Cloneable {

    /**
     * Gets the String that is placed in front of a prefixed message.
     * @return the prefix
     */
    public abstract String prefix();

    /**
     * Gets the String that is defaulted to when an input is null.
     * @return the default message
     */
    public abstract String unknown();

    /**
     * Prefixes and colors the string at the given path, but the message begins with {@link ChatColor#RED}
     * @param message the raw message
     * @return the prefixed and colored message, beginning with the color RED until it is overwritten.
     */
    public String error(String message) {
        return Optional.ofNullable(message)
                .map(msg -> color(prefix()) + ChatColor.RED + msg)
                .orElse(unknown());
    }

    /**
     * Translates the '&' alternate color code symbol.
     * @param path the path to the raw message in the configuration
     * @return the colored message
     */
    public String color(String message) {
        return Optional.ofNullable(message)
                .map(msg -> ChatColor.translateAlternateColorCodes('&', msg))
                .orElse(unknown());
    }

    /**
     * Appends the prefix and translates color codes.
     * @param path the path to the String in the configuration
     * @return the message prefixed and colored.
     */
    public String titled(String message) {
        return Optional.ofNullable(message)
                .map(prefix()::concat)
                .map(this::color)
                .orElse(unknown());
    }

    static <T extends LanguageFile> T empty(Class<T> clazz) {
        try {
            Constructor<T> constructor = clazz.getDeclaredConstructor();
            constructor.setAccessible(true);
            return constructor.newInstance();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

}
