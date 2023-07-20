package com.serwylo.babyphone

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import io.github.tonnyl.whatsnew.WhatsNew
import io.github.tonnyl.whatsnew.item.WhatsNewItem
import io.github.tonnyl.whatsnew.util.PresentationOption

object Changelog {

    @JvmStatic
    fun show(activity: AppCompatActivity) {

        val whatsNew = buildDialog(activity)

        // Only show when upgrading Lexica for the first time, not when we first open Lexica from
        // a fresh install.
        whatsNew.presentationOption = if (isFirstRun(activity)) PresentationOption.NEVER else PresentationOption.IF_NEEDED
        whatsNew.presentAutomatically(activity)

        rememberLexicaHasRun(activity)

    }

    private fun buildDialog(context: Context): WhatsNew {

        return WhatsNew.newInstance(
            WhatsNewItem(
                "Add your own contacts!",
                "Personalise your baby phone via the settings to add family and friends for your baby to talk to.\n\nCompletely private, no photo or recording will ever leave your phone.",
                R.drawable.ic_person_add,
            ),
        ).apply {
            titleText = context.getString(R.string.whats_new__title)
            buttonText = context.getString(R.string.whats_new__continue)
        }

    }

    private fun isFirstRun(context: Context): Boolean {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        return !prefs.getBoolean(HAS_RUN, false)
    }

    private fun rememberLexicaHasRun(context: Context) {
        PreferenceManager.getDefaultSharedPreferences(context)
            .edit()
            .putBoolean(HAS_RUN, true)
            .apply()
    }

    private const val HAS_RUN = "lexica-has-run-before"

}