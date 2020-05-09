package net.lotushq.languages;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;

import java.lang.reflect.Constructor;
import java.util.Optional;

public abstract class LanguageFile extends YamlConfiguration implements Cloneable {

    /**
     * Gets the String that is placed in front of a prefixed message.
     *
     * @return the prefix
     */
    public abstract String prefix();

    /**
     * Gets the String that is defaulted to when an input is null.
     *
     * @return the default message
     */
    public abstract String unknown();

    /**
     * Prefixes and colors the string at the given path, but the message begins with {@link ChatColor#RED}
     *
     * @param message the raw message
     * @return the prefixed and colored message, beginning with the color RED until it is overwritten.
     */
    public String error(String message) {
        return Optional.ofNullable(message)
                .map(msg -> color(prefix() + ChatColor.RED + msg))
                .orElse(unknown());
    }

    /**
     * Translates the '&' alternate color code symbol.
     *
     * @param message the raw text
     * @return the colored message
     */
    public String color(String message) {
        return Optional.ofNullable(message)
                .map(msg -> ChatColor.translateAlternateColorCodes('&', msg))
                .orElse(unknown());
    }

    /**
     * Appends the prefix and translates color codes.
     *
     * @param message the raw message
     * @return the message prefixed and colored.
     */
    public String titled(String message) {
        return Optional.ofNullable(message)
                .map(prefix()::concat)
                .map(this::color)
                .orElse(unknown());
    }

    /**
     * Transforms a name to show possession. By default this method only
     * supports adding an apostrophe (and an s, if applicable), as in an
     * English Locale.
     * <p>
     * Whether or not apostrophe possession is used is based on the
     * "apostrophe_possession" boolean value in the LanguageFile.
     *
     * @param name the name to edit
     * @return the name, in possessive form if applicable.
     */
    public String possessive(String name) {
        return !getBoolean("apostrophe_possession", true) ? name :
                (name.endsWith("s") ? name + "'" : name + "'s");
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
