package com.serwylo.babyphone.db.migrations

import android.content.Context
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.serwylo.babyphone.db.AppDatabase
import com.serwylo.babyphone.db.ContactDao
import com.serwylo.babyphone.db.entities.Contact
import com.serwylo.babyphone.db.entities.Recording
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

fun makeDatabaseSeeder(context: Context) = object: RoomDatabase.Callback() {
    override fun onCreate(db: SupportSQLiteDatabase) {
        val dao = AppDatabase.getInstance(context).contactDao()
        GlobalScope.launch(Dispatchers.IO) {
            defaultContacts.forEach { createDefaultContact(dao, it) }
        }
    }
}

private fun createDefaultContact(dao: ContactDao, contact: InitContact) {

    val id = dao.insert(
        Contact(
            contact.name,
            "file:///android_assets/${contact.avatar}",
            isEnabled = true,
            isDefault = true,
        )
    )

    dao.insertAll(
        contact.sounds.map {
            Recording(
                contactId = id,
                soundFilePath = "file:///android_assets/$it",
            )
        }
    )
}

private data class InitContact(val name: String, val avatar: String, val sounds: List<String>)

private val defaultContacts = listOf(
    InitContact(
        "Mum",
        "mum.jpg",
        listOf(

        )
    ),
    InitContact(
        "Mum",
        "mum.jpg",
        listOf(

        )
    ),
    InitContact(
        "Mum",
        "mum.jpg",
        listOf(

        )
    ),
)
