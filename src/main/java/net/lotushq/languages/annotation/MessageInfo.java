package net.lotushq.languages.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface MessageInfo {

    /**
     * The path in the configuration file to the message.
     * @return the path
     */
    String path();

    /**
     * The {@link FormatPreset} to apply to the raw message retrieved from the configuration.
     * @return the format preset to apply
     */
    FormatPreset format() default FormatPreset.TITLED;

    /**
     * Tells whether or not the path contains a replacement. When this is true, parameters will be consumed to
     * format the path before they are used for message replacements.
     * @return true if path contains a replacement placeholder.
     */
    boolean variablePath() default false;

}
