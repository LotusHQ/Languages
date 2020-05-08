package net.lotushq.languages;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * An annotation used to denote which language codes a
 * LanguageFile provides translations for.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface SupportedLanguages {

    /**
     * An array of language codes that this file supports.
     * @return the language codes
     */
    String[] languages();

}
