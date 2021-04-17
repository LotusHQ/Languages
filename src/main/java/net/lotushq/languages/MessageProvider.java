package net.lotushq.languages;

import net.lotushq.languages.annotation.FormatPreset;
import net.lotushq.languages.annotation.MessageInfo;

/**
 * <pre>
 * A MessageProvider bridges the gap between a configuration and your code. It is always an interface, as the
 * implementation is only ever created by a dynamic proxy. Interfaces that extend this are used to define which
 * messages are available in a configuration file AND the replacements that should be applied to the retrieved
 * messages.
 *
 * Here's a relatively straight forward example that defines a message requiring a String replacement:
 *  {@code
 *  public interface GreetingsLang extends MessageProvider {
 *
 *      @MessageInfo(path = "greetings.player", format = FormatPreset.PLAIN)
 *      String playerGreeting(String playerName);
 *
 *   }
 *  }
 * When the "playerGreeting(String)" method is called, the proxy implementation will fetch the raw message from
 * the configuration file and will use {@link String#format(String, Object...)} to apply the provided "playerName"
 * replacement.
 * </pre>
 *
 * Other objects may be used as replacements if the correct format syntax is used. For more formatting information, see
 * the <a href="https://docs.oracle.com/javase/7/docs/api/java/util/Formatter.html">Formatter documentation</a>.
 * </pre>
 */
public interface MessageProvider {

    /**
     * Gets the String that is placed in front of a prefixed message.
     *
     * @return the prefix
     */
    @MessageInfo(path = "prefix", format = FormatPreset.PLAIN)
    String prefix();

    /**
     * Gets the String that is defaulted to when an input is null.
     *
     * @return the default message
     */
    @MessageInfo(path = "unknown", format = FormatPreset.ERROR)
    String unknown();

}
