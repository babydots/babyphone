package com.serwylo.babyphone

import android.content.Context
import androidx.annotation.DrawableRes
import androidx.preference.PreferenceManager

object ContactManager {

    fun getSelectedContactName(context: Context) =
        PreferenceManager.getDefaultSharedPreferences(context).getString(PREFERENCE_NAME, null) ?: CONTACT_BABY

    fun getContact(context: Context, name: String): Contact {
        return when (name) {
            CONTACT_MUM ->
                Contact(
                    CONTACT_MUM,
                    context.getString(R.string.contact_mum),
                    RandomSoundLibrary(context, RandomSoundLibrary.babyTalk),
                    R.drawable.mum
                )

            CONTACT_DAD ->
                Contact(
                    CONTACT_DAD,
                    context.getString(R.string.contact_dad),
                    RandomSoundLibrary(context, RandomSoundLibrary.babyTalk),
                    R.drawable.baby
                )

            else ->
                Contact(
                    CONTACT_BABY,
                    context.getString(R.string.contact_baby),
                    RandomSoundLibrary(context, RandomSoundLibrary.babyTalk),
                    R.drawable.baby
                )
        }
    }

    private const val PREFERENCE_NAME = "contact"

    private const val CONTACT_BABY = "baby"
    private const val CONTACT_MUM = "mum"
    private const val CONTACT_DAD = "dad"
}

data class Contact(

    /**
     * An identifier used to refer to this contact.
     * Used by preferences and other internal systems, not for showing to the users.
     */
    val name: String,

    /**
     * Human readable name of this contact.
     */
    val label: String,

    val soundLibrary: RandomSoundLibrary,

    @DrawableRes
    val avatarDrawableId: Int,

)