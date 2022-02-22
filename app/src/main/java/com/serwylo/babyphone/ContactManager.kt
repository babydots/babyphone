package com.serwylo.babyphone

import android.content.Context
import androidx.core.content.edit
import androidx.preference.PreferenceManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

object ContactManager {

    fun getSelectedContactName(context: Context) =
        PreferenceManager.getDefaultSharedPreferences(context).getString(PREFERENCE_NAME, null) ?: CONTACT_BABY

    fun setSelectedContact(context: Context, contact: Contact) {
        PreferenceManager.getDefaultSharedPreferences(context).edit {
            putString(PREFERENCE_NAME, contact.name)
        }
    }

    suspend fun getContact(context: Context, name: String): Contact? {
        return getBuiltInContact(context, name) ?: getCustomContact(context, name)
    }

    private fun getBuiltInContact(context: Context, name: String): Contact? {
        return when (name) {
            CONTACT_MUM ->
                Contact(
                    CONTACT_MUM,
                    context.getString(R.string.contact_mum),
                    ResourceRandomSoundLibrary(context, ResourceRandomSoundLibrary.mumTalk),
                    "file:///android_asset/mum.jpg",
                )

            CONTACT_DAD ->
                Contact(
                    CONTACT_DAD,
                    context.getString(R.string.contact_dad),
                    ResourceRandomSoundLibrary(context, ResourceRandomSoundLibrary.dadTalk),
                    "file:///android_asset/dad.jpg",
                )

            CONTACT_BABY ->
                Contact(
                    CONTACT_BABY,
                    context.getString(R.string.contact_baby),
                    ResourceRandomSoundLibrary(context, ResourceRandomSoundLibrary.babyTalk),
                    "file:///android_asset/baby.jpg",
                )

            else -> null
        }
    }

    private suspend fun getCustomContact(context: Context, name: String): Contact? {
        return loadCustomContacts(context).firstOrNull { it.name == name }
    }

    suspend fun getContacts(context: Context): List<Contact> {
        return listOf(
            getBuiltInContact(context, CONTACT_BABY)!!,
            getBuiltInContact(context, CONTACT_MUM)!!,
            getBuiltInContact(context, CONTACT_DAD)!!,
        ) + loadCustomContacts(context)
    }

    private suspend fun loadCustomContacts(context: Context): List<Contact> = withContext(Dispatchers.IO) {
        val contactsDir = context.getExternalFilesDir("Contacts") ?: return@withContext emptyList()

        val contactDirList = contactsDir.listFiles { file -> file.nameWithoutExtension.toIntOrNull() != null } ?: return@withContext emptyList()
        contactDirList.map { dir ->
            val nameFile = File(dir, "name.txt")

            Contact(
                name = "custom_${dir.name}",
                label = if (nameFile.exists()) nameFile.readText() else "",
                RecordedRandomSoundLibrary(context, dir.listFiles { file -> file.extension == "3gp"}?.toList() ?: emptyList()),
                "file://${File(dir, "photo.small.jpg").absolutePath}",
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

    val soundLibrary: RandomSoundLibrary<*>,

    /**
     * To be consumed by [com.squareup.Picasso#load].
     */
    val avatarPath: String? = null,

)