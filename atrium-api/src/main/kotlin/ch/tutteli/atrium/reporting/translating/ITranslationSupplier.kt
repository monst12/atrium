package ch.tutteli.atrium.reporting.translating

import java.util.*

/**
 * A supplier of translations for [ITranslatable]s for particular [Locale]s.
 */
interface ITranslationSupplier {

    /**
     * Returns the translation for the given [translatable] for the given [locale] or null if it does not have any.
     *
     * @return The translation or null if no translation was found.
     */
    fun get(translatable: ITranslatable, locale: Locale): String?
}
