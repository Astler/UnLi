package dev.astler.unlib

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Resources
import androidx.core.os.ConfigurationCompat
import androidx.preference.PreferenceManager
import dev.astler.unlib.core.R

open class PreferencesTool(context: Context?) {

    companion object {
        const val appThemeKey = "appTheme"
        const val vibrationKey = "keyVibration"

        const val appLocaleKey = "appLocale"
        const val appLocaleDefault = "system"
        const val appLocaleModeKey = "appLocaleMode"
        const val appLocaleModeDefault = false

        const val textSizeKey = "textSizeNew"
        const val textSizeDefault = "18"

        const val useEnglishKey = "useEnglish"
        const val firstStartKey = "firstStart"
        const val dayWithoutAdsKey = "dayWithoutAds"
    }

    private var mPreferences = PreferenceManager.getDefaultSharedPreferences(context)

    open fun loadDefaultPreferences(context: Context, pNewPreferencesArray: IntArray = intArrayOf()) {
        PreferenceManager.setDefaultValues(context, R.xml.prefs, false)

        pNewPreferencesArray.forEach {
            PreferenceManager.setDefaultValues(context, it, false)
        }
    }

    private fun getPreferences(): SharedPreferences = mPreferences

    var mTextSize: Float
        get() = (getPreferences().getString(textSizeKey, textSizeDefault))?.toFloat() ?: 18f
        set(value) {
            edit(textSizeKey, value.toString())
        }

    var mIsDarkTheme: Boolean
        get() = appTheme == "dark"
        set(value) {
            if (value)
                edit(appThemeKey, "dark")
        }

    var mIsSystemTheme: Boolean
        get() = appTheme == "system"
        set(value) {
            if (value)
                edit(appThemeKey, "system")
        }

    var vibrateOnClick: Boolean
        get() = getPreferences().getBoolean(vibrationKey, false)
        set(value) {
            if (value)
                edit(vibrationKey, "system")
        }

    var isSystemLanguage: Boolean
        get() = appLanguage == "system"
        set(value) {
            if (value)
                edit(appLocaleKey, "system")
        }

    var appTheme: String
        get() = getPreferences().getString(appThemeKey, "system") ?: "system"
        set(value) {
            edit(appThemeKey, value)
        }

    var appLanguage: String
        get() = getPreferences().getString(appLocaleKey, "system") ?: "system"
        set(value) {
            edit(appLocaleKey, value)
        }

    var isFirstStart: Boolean
        get() = getPreferences().getBoolean(firstStartKey, true)
        set(value) {
            edit(firstStartKey, value)
        }

    var dayWithoutAds: Int
        get() = getPreferences().getInt(dayWithoutAdsKey, -1)
        set(value) {
            edit(dayWithoutAdsKey, value)
        }

    var chooseLanguageManually: Boolean
        get() = getPreferences().getBoolean(appLocaleModeKey, appLocaleModeDefault)
        set(value) {
            edit(appLocaleModeKey, value)
        }

    var userLanguage: String
        get() = getPreferences().getString(appLocaleKey, appLocaleDefault) ?: appLocaleDefault
        set(value) {
            edit(appLocaleKey, value)
        }

    fun isFirstStartForVersion(versionCode: Int) =
        getPreferences().getBoolean("firstStartVersion$versionCode", true)

    fun addListener(listener: SharedPreferences.OnSharedPreferenceChangeListener) {
        getPreferences().registerOnSharedPreferenceChangeListener(listener)
    }

    fun unregisterListener(listener: SharedPreferences.OnSharedPreferenceChangeListener) {
        getPreferences().unregisterOnSharedPreferenceChangeListener(listener)
    }

    fun setFirstStartForVersion(versionCode: Int) {
        edit("firstStartVersion$versionCode", false)
    }

    open fun preferencesToDefault() {
        mIsSystemTheme = true
        mTextSize = 18f
        userLanguage = ConfigurationCompat.getLocales(Resources.getSystem().configuration).get(0).toString()
    }

    fun edit(name: String, type: Any) {
        val editor = getPreferences().edit()

        when (type) {
            is Boolean -> editor.putBoolean(name, type)
            is Int -> editor.putInt(name, type)
            is Float -> editor.putFloat(name, type)
            is String -> editor.putString(name, type)
            is Long -> editor.putLong(name, type)
        }

        editor.apply()
    }

    fun getBoolean(pKey: String, pDefValue: Boolean = true): Boolean = getPreferences().getBoolean(pKey, pDefValue)
    fun getString(pKey: String, pDefValue: String = ""): String = getPreferences().getString(pKey, pDefValue)?:pDefValue
    fun getInt(pKey: String, pDefValue: Int = 0): Int = getPreferences().getInt(pKey, pDefValue)
    fun getLong(pKey: String, pDefValue: Long = 0L): Long = getPreferences().getLong(pKey, pDefValue)

}
