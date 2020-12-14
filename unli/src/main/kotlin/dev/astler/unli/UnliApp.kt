package dev.astler.unli

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.multidex.MultiDexApplication
import dev.astler.unli.utils.log

val preferencesTool: PreferencesTool by lazy {
    UnliApp.prefs
}

open class UnliApp : MultiDexApplication() {

    companion object {
        lateinit var prefs: PreferencesTool

        private lateinit var applicationInstance: UnliApp

        @Synchronized
        fun getInstance(): UnliApp {
            return applicationInstance
        }
    }

    override fun onCreate() {
        super.onCreate()
        prefs = PreferencesTool(this)
        applicationInstance = this
        createNotificationChannels()
    }

    open fun createNotificationChannels() {}

    fun initAppLanguage(context: Context) {
        AppSettings.loadLocale(
                context
        )
    }

    private fun createNotificationChannel(pName: String = packageName, pDescription: String = "", pChannelId: String = "unli_default") {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(pChannelId, pName, importance).apply {
                description = pDescription
            }

            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}
