package com.lamontlabs.quantravision.system

import android.content.Context
import android.content.res.Configuration
import java.util.Locale

/**
 * LocaleManager
 * Forces app locale deterministically (default EN) with optional override.
 */
object LocaleManager {

    private var forced: Locale? = null

    fun setLocale(context: Context, languageTag: String?) {
        forced = languageTag?.let { Locale.forLanguageTag(it) }
        apply(context)
    }

    fun apply(context: Context) {
        val conf = Configuration(context.resources.configuration)
        conf.setLocale(forced ?: Locale.ENGLISH)
        @Suppress("DEPRECATION")
        context.resources.updateConfiguration(conf, context.resources.displayMetrics)
    }
}
