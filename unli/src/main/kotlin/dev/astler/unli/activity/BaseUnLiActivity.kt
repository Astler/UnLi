package dev.astler.unli.activity

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import android.os.Bundle
import android.view.MenuItem
import androidx.annotation.StyleRes
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import com.google.android.gms.ads.*
import com.google.android.gms.ads.RequestConfiguration.TAG_FOR_CHILD_DIRECTED_TREATMENT_TRUE
import com.google.android.gms.ads.reward.RewardItem
import com.google.android.gms.ads.reward.RewardedVideoAd
import com.google.android.gms.ads.reward.RewardedVideoAdListener
import com.google.android.material.navigation.NavigationView
import dev.astler.unli.*
import dev.astler.unli.R
import dev.astler.unli.interfaces.ActivityInterface
import dev.astler.unli.utils.canShowAds
import dev.astler.unli.utils.moreApps
import dev.astler.unli.utils.rateApp
import dev.astler.unli.utils.showInfoDialog
import java.util.*
import kotlin.collections.ArrayList

abstract class BaseUnLiActivity : AppCompatActivity(),
    NavigationView.OnNavigationItemSelectedListener,
    SharedPreferences.OnSharedPreferenceChangeListener, ActivityInterface, RewardedVideoAdListener {

    lateinit var mRewardedVideo: RewardedVideoAd
    private lateinit var mInterstitialAd: InterstitialAd
    lateinit var mPreferencesTool: PreferencesTool
    private var showAdAfterLoading = false
    var infoDialog: AlertDialog? = null
    var mActiveFragment: Fragment? = null

    open fun initPreferencesTool(): PreferencesTool = PreferencesTool(this)

    override fun getActivityContext() = this

    open var mInterstitialAdId = ""
    open var mRewardedAdId = ""
    open var mAppMenuId = 0

    open fun updateNavigationMenu() {}

    override fun setCurrentFragment(fragment: Fragment) {
        mActiveFragment = fragment
    }

    open fun getTestDevicesList(): ArrayList<String> {
        return arrayListOf(AdRequest.DEVICE_ID_EMULATOR, "46BCDEE9C1F5ED2ADF3A5DB3889DDFB5")
    }

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(AppSettings.loadLocale(newBase))
    }

    open fun getAdRequest(): AdRequest{
        return AdRequest.Builder().build()
    }

    open var setTagForChildDirectedTreatment = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        UnliApp.getInstance().initAppLanguage(this)

        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)

        when {
            preferencesTool.isSystemTheme -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
                delegate.applyDayNight()
            }
            preferencesTool.isDarkTheme -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                delegate.applyDayNight()
            }
            else -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                delegate.applyDayNight()
            }
        }

        val testDevices = ArrayList<String>()
        testDevices.add(AdRequest.DEVICE_ID_EMULATOR)
        testDevices.addAll(getTestDevicesList())

        val requestConfiguration = RequestConfiguration.Builder()
            .setTestDeviceIds(testDevices)

        if (setTagForChildDirectedTreatment) {
            requestConfiguration.setTagForChildDirectedTreatment(TAG_FOR_CHILD_DIRECTED_TREATMENT_TRUE)
        }

        MobileAds.setRequestConfiguration(requestConfiguration.build())

        setDefaultPreferences()

        mPreferencesTool = initPreferencesTool()

        mRewardedVideo = MobileAds.getRewardedVideoAdInstance(this)

        if (canShowAds() && mRewardedAdId.isNotEmpty()) {
            mRewardedVideo.rewardedVideoAdListener = this
            mRewardedVideo.loadAd(mRewardedAdId, getAdRequest())
        }

        mInterstitialAd = InterstitialAd(this)

        if (mInterstitialAdId.isNotEmpty()) {

            mInterstitialAd.adUnitId = mInterstitialAdId
            mInterstitialAd.loadAd(getAdRequest())

            mInterstitialAd.adListener = object : AdListener() {
                override fun onAdClosed() {
                    mInterstitialAd.loadAd(getAdRequest())
                }
            }
        }
    }

    override fun showInterstitialAd() {
        if (mInterstitialAd.isLoaded) {
            mInterstitialAd.show()
        } else {
            if (!mInterstitialAd.isLoading)
                mInterstitialAd.loadAd(getAdRequest())
        }
    }

    override fun showRewardAd() {
        if (mRewardedVideo.isLoaded)
            mRewardedVideo.show()
        else {
            infoDialog = showInfoDialog(R.string.just_a_minute, R.string.ad_is_loading)
            infoDialog?.show()

            showAdAfterLoading = true
        }
    }

    open fun hideAd() {}

    open fun setDefaultPreferences() {
        PreferenceManager.setDefaultValues(this, R.xml.prefs, false)
    }

    fun loadTheme(@StyleRes lightThemeId: Int, @StyleRes darkThemeId: Int) {
        if (mPreferencesTool.isSystemTheme) {
            when (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
                Configuration.UI_MODE_NIGHT_YES -> setTheme(darkThemeId)
                Configuration.UI_MODE_NIGHT_NO -> setTheme(lightThemeId)
            }
        } else {
            if (mPreferencesTool.isDarkTheme) {
                setTheme(darkThemeId)
            } else {
                setTheme(lightThemeId)
            }
        }
    }

    open fun navigationViewInflateMenus(navigationView: NavigationView) {
        navigationView.menu.clear()

        if (canShowAds())
            navigationView.inflateMenu(R.menu.ad_menu)

        if (mAppMenuId != 0)
            navigationView.inflateMenu(mAppMenuId)

        navigationView.inflateMenu(R.menu.base_activity_drawer)
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.adItem -> showRewardAd()
            R.id.rate_app -> this.rateApp()
            R.id.more_apps -> this.moreApps()
            R.id.settings -> navToSettingsFragment()
            R.id.about -> navToAboutFragment()
        }

        return true
    }

    open fun navToSettingsFragment() {}
    open fun navToAboutFragment() {}

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        if (key == PreferencesTool.appThemeKey || key == PreferencesTool.appLocaleModeKey || key == PreferencesTool.appLocaleKey) {
            recreate()
        }

        if (key == PreferencesTool.dayWithoutAdsKey) {
            hideAd()
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)

        val preferencesTool = PreferencesTool(this)

        if (preferencesTool.isSystemTheme) {
            when (newConfig.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
                Configuration.UI_MODE_NIGHT_NO -> {
                    recreate()
                }
                Configuration.UI_MODE_NIGHT_YES -> {
                    recreate()
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        mPreferencesTool.addListener(this)
    }

    override fun onStop() {
        super.onStop()
        mPreferencesTool.unregisterListener(this)
    }


//    fun updateBackgroundColor(color: Int = appPrefs.backgroundColor) {
//        window.decorView.setBackgroundColor(color)
//    }
//
//    fun updateActionbarColor(color: Int = appPrefs.primaryColor) {
//        supportActionBar?.setBackgroundDrawable(ColorDrawable(color))
//        updateActionBarTitle(supportActionBar?.title.toString(), color)
//        updateStatusbarColor(color)
//        //setTaskDescription(ActivityManager.TaskDescription(null, null, color))
//    }
//
//    fun updateStatusbarColor(color: Int) {
//        window.statusBarColor = color.darkenColor()
//    }

    override fun onRewardedVideoAdClosed() {}
    override fun onRewardedVideoAdLeftApplication() {}
    override fun onRewardedVideoAdLoaded() {
        if (showAdAfterLoading)
            showRewardAd()

        infoDialog?.dismiss()
    }

    override fun onRewardedVideoAdOpened() {}
    override fun onRewardedVideoCompleted() {}

    override fun onRewarded(p0: RewardItem?) {
        val preferencesTool = PreferencesTool(this@BaseUnLiActivity)
        preferencesTool.dayWithoutAds =
            GregorianCalendar.getInstance().get(GregorianCalendar.DATE)
    }

    override fun onRewardedVideoStarted() {}
    override fun onRewardedVideoAdFailedToLoad(p0: Int) {}
}