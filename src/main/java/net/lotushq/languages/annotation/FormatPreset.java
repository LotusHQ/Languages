package net.lotushq.languages.annotation;

import org.bukkit.ChatColor;

public enum FormatPreset {

    /**
     * The message will not be appended with any text or color.
     */
    PLAIN,

    /**
     * The message will be appended with the configured prefix text. The color depends on the prefix.
     */
    TITLED,

    /**
     * The message will be appended with the configured prefix text and {@link org.bukkit.ChatColor#RED}
     */
    ERROR;

   public String format(String prefix, String message) {
        if (this == PLAIN) return ChatColor.translateAlternateColorCodes('&', message);

        String titled = prefix + message;
        if (this == ERROR) titled += ChatColor.RED;

        return ChatColor.translateAlternateColorCodes('&', titled);
   }

}
