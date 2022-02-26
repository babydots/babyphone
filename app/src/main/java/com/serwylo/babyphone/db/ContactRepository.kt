package com.serwylo.babyphone.db

import android.content.Context
import androidx.core.content.edit
import androidx.preference.PreferenceManager
import com.serwylo.babyphone.db.ContactDao
import com.serwylo.babyphone.db.entities.Contact
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class ContactRepository(private val context: Context, private val dao: ContactDao) {

    suspend fun selectContact(contact: Contact) = withContext(Dispatchers.IO) {
        dao.changeCurrentContact(contact.id)
    }

}