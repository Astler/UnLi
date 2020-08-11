package dev.astler.unli.utils

import android.content.Context
import android.os.Build
import java.util.*

object LocalizationUtil {

    @SuppressWarnings("Deprecated in Android 17")
    fun applyLanguage(context: Context, locale: Locale): Context {
        val configuration = context.resources.configuration
        val displayMetrics = context.resources.displayMetrics

        Locale.setDefault(locale)

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            configuration.setLocale(locale)
            context.createConfigurationContext(configuration)
        } else {
            configuration.locale = locale
            context.resources.updateConfiguration(configuration, displayMetrics)
            context
        }
    }
}