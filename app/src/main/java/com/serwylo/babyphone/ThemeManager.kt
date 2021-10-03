package com.serwylo.babyphone

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.preference.PreferenceManager

/**
 * This class is based on GPLv3 licensed code from F-Droid client:
 *
 * https://gitlab.com/fdroid/fdroidclient/-/blob/master/app/src/main/java/org/fdroid/fdroid/FDroidApp.java
 */
object ThemeManager {

    private var currentTheme: String? = null

    fun getCurrentTheme() = currentTheme

    /**
     * Force reload the activity to make theme changes take effect.
     */
    fun forceRestartActivityToRetheme(activity: Activity) {
        // when launched as LAUNCHER will be null.
        val intent = activity.intent ?: return
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)

        activity.finish()
        activity.overridePendingTransition(0, 0)
        activity.startActivity(intent)
        activity.overridePendingTransition(0, 0)
    }

    fun rememberTheme(context: Context?) {
        currentTheme = PreferenceManager.getDefaultSharedPreferences(context).getString(PREFERENCE_NAME, THEME_COLOURS)
    }

    fun applyTheme(activity: Activity): String? {
        activity.setTheme(getCurThemeResId(activity))
        return currentTheme
    }

    private fun getCurThemeResId(context: Context): Int {
        if (currentTheme == null) {
            rememberTheme(context)
        }

        return when (currentTheme) {
            THEME_DARK -> R.style.Theme_BabyPhone_Dark
            THEME_LIGHT -> R.style.Theme_BabyPhone_Light
            else -> R.style.Theme_BabyPhone_Colours
        }
    }

    const val PREFERENCE_NAME = "theme"
    private const val THEME_LIGHT = "light"
    private const val THEME_DARK = "dark"
    private const val THEME_COLOURS = "colours"
}